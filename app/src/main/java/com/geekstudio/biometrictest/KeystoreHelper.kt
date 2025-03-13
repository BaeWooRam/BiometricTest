package com.geekstudio.biometrictest

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.SecureRandom
import java.security.Signature

object KeystoreHelper {
    const val KEY_ALIAS = "biometric_key"
    private const val KEY_STORE_PROVIDER_NAME = "AndroidKeyStore"
    private val keyStore: KeyStore = KeyStore.getInstance(KEY_STORE_PROVIDER_NAME)

    fun generateKeyPair(keyAlias: String = KEY_ALIAS) {
        kotlin.runCatching {
            val secureRandom = SecureRandom.getInstanceStrong()
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE_PROVIDER_NAME
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setKeySize(2048)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setUserAuthenticationRequired(true) // 생체 인증 필수
                .setInvalidatedByBiometricEnrollment(true) // 새로운 생체 정보 등록 시 키 무효화
                .build()

            keyPairGenerator.initialize(keyGenParameterSpec, secureRandom)
            keyPairGenerator.generateKeyPair()
            Log.d(KEY_ALIAS, "🔐 Keystore 키 생성 완료!")
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    fun getKeyPair(keyAlias: String = KEY_ALIAS): KeyPair? {
        return kotlin.runCatching {
            keyStore.load(null)
            val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.PrivateKeyEntry
            entry?.let {
                KeyPair(it.certificate.publicKey, it.privateKey)
            }
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull()
    }

    fun isKeyValid(keyAlias: String = KEY_ALIAS): Boolean {
        keyStore.load(null)
        return keyStore.containsAlias(keyAlias) // 🔥 키 존재 여부 확인
    }

    fun isKeyAccessible(keyAlias: String = KEY_ALIAS): Boolean {
        return try {
            keyStore.load(null)
            val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.PrivateKeyEntry
            entry != null // ✅ 키가 정상적으로 가져와지면 true
        } catch (e: KeyStoreException) {
            false // ❌ 키가
        // 없거나 접근 불가능하면 false
        }
    }

    fun isKeyUsable(keyAlias: String = KEY_ALIAS): Boolean {
        return try {
            keyStore.load(null)
            val entry = keyStore.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(entry.privateKey) // 🔥 무효화된 경우 예외 발생!
            true
        } catch (e: KeyPermanentlyInvalidatedException) {
            false // ❌ 키가 무효화됨
        } catch (e: Exception) {
            false // ❌ 키가 아예 존재하지 않거나 다른 문제 발생
        }
    }
}
