package com.geekstudio.biometrictest

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject

interface Biometric {
    fun initPrivateKey()
    fun getBiometricPromptAuthenticationCallback(): BiometricPrompt.AuthenticationCallback
    fun getPromptInfo(): BiometricPrompt.PromptInfo
    fun authenticate(cryptoObject: CryptoObject? = null)
}