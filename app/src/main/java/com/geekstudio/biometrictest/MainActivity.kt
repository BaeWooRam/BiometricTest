package com.geekstudio.biometrictest

import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private var biometricPrompt: BiometricPrompt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBiometricPopUp()

        val manager = BiometricManager.from(this@MainActivity)
        when (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Toast.makeText(this@MainActivity, "status = BIOMETRIC_SUCCESS", Toast.LENGTH_SHORT).show()
                generateSecretKey(
                    KeyGenParameterSpec.Builder(
                        KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setUserAuthenticationRequired(true)
                        // Invalidate the keys if the user has registered a new biometric
                        // credential, such as a new fingerprint. Can call this method only
                        // on Android 7.0 (API level 24) or higher. The variable
                        // "invalidatedByBiometricEnrollment" is true by
                        // default.
                        .setInvalidatedByBiometricEnrollment(true)
                        .build()
                )
                showBiometric()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this@MainActivity, "status = BIOMETRIC_ERROR_NONE_ENROLLED", Toast.LENGTH_SHORT).show()
            }

            else -> Toast.makeText(this@MainActivity, "status = else type", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initBiometricPopUp() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    when(result.authenticationType){
                        BiometricPrompt.AUTHENTICATION_RESULT_TYPE_UNKNOWN -> {
                            //사용자가 알 수 없는 방법을 통해 인증한 경우 보고되는 인증 유형입니다 .
                            //이 값은 최신 API와의 부분적 비호환성으로 인해 이전 Android 버전에서 반환될 수 있습니다. 이는 반드시 사용자가 AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL및 로 표시된 방법 이외의 방법으로 인증되었음을 의미하지는 않습니다 AUTHENTICATION_RESULT_TYPE_BIOMETRIC.
                            Log.d(TAG, "onAuthenticationSucceeded authenticationType = AUTHENTICATION_RESULT_TYPE_UNKNOWN")
                        }
                        BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL -> {
                            //사용자가 기기 PIN, 패턴 또는 비밀번호를 입력하여 인증한 경우 보고되는 인증 유형입니다 .
                            Log.d(TAG, "onAuthenticationSucceeded authenticationType = AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL")
                        }
                        BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC -> {
                            //사용자가 생체 인식(예: 지문 또는 얼굴)을 제시하여 인증한 경우에 의해 보고되는 인증 유형입니다 .
                            Log.d(TAG, "onAuthenticationSucceeded authenticationType = AUTHENTICATION_RESULT_TYPE_BIOMETRIC")
                        }
                    }

                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
    }

    private fun showBiometric(){
        val build = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG)
//            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()

        val cipher = getCipher()
        val secretKey = getSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val bioCryptoObject = BiometricPrompt.CryptoObject(cipher)
        biometricPrompt?.authenticate(build, bioCryptoObject)
    }

    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        Log.d(TAG, "keyStore = $keyStore")
        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        Log.d(TAG, "size = ${keyStore.size()}")
        return keyStore.getKey(KEY_NAME, null) as? SecretKey
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
    }

    companion object {
        const val KEY_NAME = "TEST_BIOMETRIC_KEY"
        const val TIME_OUT = 0
    }
}