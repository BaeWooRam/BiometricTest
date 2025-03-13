package com.geekstudio.biometrictest

import androidx.biometric.BiometricPrompt.CryptoObject
import java.security.Signature
import java.util.Base64

object SignatureHelper {
    private val signatureInstance = Signature.getInstance("SHA256withRSA")
    private var cryptoObject: CryptoObject? = null

    fun initSign() {
        kotlin.runCatching {
            val keyPair = KeystoreHelper.getKeyPair()
            signatureInstance.initSign(keyPair!!.private)
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull()
    }

    fun signData(data: String): String? {
        return kotlin.runCatching {
            signatureInstance.update(data.toByteArray())
            val signedData = signatureInstance.sign()
            Base64.getEncoder().encodeToString(signedData)
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull()
    }

    fun getCryptoObject(): CryptoObject {
        return synchronized(this) {
            cryptoObject ?: CryptoObject(signatureInstance)
                .also { cryptoObject = it }
        }
    }

    fun verifyData(data: String, signatureStr: String): Boolean {
        return kotlin.runCatching {
            val keyPair = KeystoreHelper.getKeyPair()

            signatureInstance.initVerify(keyPair!!.public)
            signatureInstance.update(data.toByteArray())

            val signatureBytes = Base64.getDecoder().decode(signatureStr)
            signatureInstance.verify(signatureBytes)
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull() ?: false
    }
}