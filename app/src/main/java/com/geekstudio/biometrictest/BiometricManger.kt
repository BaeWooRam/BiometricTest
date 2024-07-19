package com.geekstudio.biometrictest

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.BLOCK_MODE_CBC
import android.security.keystore.KeyProperties.ENCRYPTION_PADDING_PKCS7
import android.security.keystore.KeyProperties.KEY_ALGORITHM_AES
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.geekstudio.biometrictest.MainActivity.Companion.DEFAULT_KEY_NAME
import java.io.IOException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class BiometricManger(
    private val context: Context
) {
    private var tag = this@BiometricManger.javaClass.simpleName
    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    private var biometricPrompt: BiometricPrompt? = null
    private var defaultCipher: Cipher? = null

    fun setupKeyStoreAndKeyGenerator(keyStoreName: String, algorithm: String = KEY_ALGORITHM_AES) {
        kotlin.runCatching {
            keyStore = KeyStore.getInstance(keyStoreName)
            keyGenerator = KeyGenerator.getInstance(algorithm, keyStoreName)
        }.onFailure { e ->
            Log.d(tag, "setupKeyStoreAndKeyGenerator onFailure = $e")
        }
    }

    fun canAuthenticate(authenticators: Int) = BiometricManager.from(context).canAuthenticate(authenticators)

    fun createKey(keyName: String, invalidatedByBiometricEnrollment: Boolean = false) {
        kotlin.runCatching {
            keyStore!!.load(null)

            val keyProperties = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            val builder = KeyGenParameterSpec.Builder(keyName, keyProperties)
                .setBlockModes(BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7)
                .setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)

            keyGenerator!!.run {
                init(builder.build())
                generateKey()
            }
        }.onFailure { e ->
            Log.d(tag, "createKey onFailure error = $e")
        }
    }

    fun setupCiphers() {
        kotlin.runCatching {
            val cipherString = "$KEY_ALGORITHM_AES/$BLOCK_MODE_CBC/$ENCRYPTION_PADDING_PKCS7"
            defaultCipher = Cipher.getInstance(cipherString)
        }.onFailure { e ->
            Log.d(tag, "setupCiphers onFailure error = $e")
        }
    }

    fun tryEncrypt(secretMessage: String): ByteArray? {
        return kotlin.runCatching {
            defaultCipher!!.doFinal(secretMessage.toByteArray())
        }.onFailure { e ->
            Log.d(tag, "tryEncrypt onFailure error = $e")
        }.getOrNull()
    }

    fun initCipher(keyName: String): Boolean {
        val isException = kotlin.runCatching {
            keyStore!!.load(null)
            val secretKey = keyStore!!.getKey(keyName, null) as SecretKey
            defaultCipher!!.init(Cipher.ENCRYPT_MODE, secretKey)
        }.onFailure { e ->
            when(e){
                is KeyPermanentlyInvalidatedException -> {
                    createKey(keyName, true)
                    Log.d(tag, "initCipher KeyPermanentlyInvalidatedException")
                }
                else -> Log.d(tag, "initCipher onFailure error = $e")
            }
        }.exceptionOrNull()

        return isException == null
    }

    fun setUpBiometricPopUp(biometricPrompt: BiometricPrompt) {
        this@BiometricManger.biometricPrompt = biometricPrompt
    }

    fun showBiometric(promptInfo: BiometricPrompt.PromptInfo) {
        kotlin.runCatching {
            biometricPrompt?.authenticate(promptInfo, BiometricPrompt.CryptoObject(defaultCipher!!))
        }.onFailure { e ->
            Log.d(tag, "showBiometric onFailure error = $e")
        }
    }

}