package com.geekstudio.biometrictest

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

open class BiometricImp : Biometric {
    private val tag = javaClass.simpleName

    //Biometric
    private var biometricManager: BiometricManager? = null
    private var prompt: BiometricPrompt? = null
    private var promptInfo: BiometricPrompt.PromptInfo? = null

    //etc
    private var privateKeyManager: PrivateKeyManager? = null
    private var executorInstance: Executor? = null

    fun setUp(fragment: Fragment) {
        setUp(fragment.requireContext())
        prompt = BiometricPrompt(
            fragment,
            executorInstance!!,
            getBiometricPromptAuthenticationCallback()
        )
    }

    fun setUp(fragmentActivity: FragmentActivity) {
        setUp(fragmentActivity.applicationContext)
        prompt = BiometricPrompt(
            fragmentActivity,
            executorInstance!!,
            getBiometricPromptAuthenticationCallback()
        )
    }

    private fun setUp(context: Context) {
        setUpExecutorInstance(context)
        promptInfo = getPromptInfo()
        biometricManager = BiometricManager.from(context)
    }

    private fun setUpExecutorInstance(context: Context) {
        synchronized(this) {
            executorInstance ?: ContextCompat.getMainExecutor(context)
                .also { executorInstance = it }
        }
    }

    override fun initPrivateKey() {
        TODO("Not yet implemented")
    }

    override fun getBiometricPromptAuthenticationCallback(): BiometricPrompt.AuthenticationCallback =
        object : BiometricPrompt.AuthenticationCallback() {
            /**
             * 복구할 수 없는 오류가 발생하여 인증이 중지된 경우 호출됩니다.
             */
            override fun onAuthenticationError(
                errorCode: Int, errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(tag, "onAuthenticationError errorCode = $errorCode, errString = $errString}")
            }

            /**
             * 생체 인식(예: 지문, 얼굴 등)이 인식되면 호출되어 사용자가 성공적으로 인증되었음을 나타냅니다.
             */
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)
                Log.d(tag, "onAuthenticationSucceeded cryptoObject = ${result.cryptoObject}")

                //기기 사용자 인증 정보와 생체 인식 사용자 인증 정보 중 어떤 정보가 사용되었는지 확인할 수 있습니다.
                when (result.authenticationType) {
                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_UNKNOWN -> {
                        //사용자가 알 수 없는 방법을 통해 인증한 경우 보고되는 인증 유형입니다 .
                        //이 값은 최신 API와의 부분적 비호환성으로 인해 이전 Android 버전에서 반환될 수 있습니다. 이는 반드시 사용자가 AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL및 로 표시된 방법 이외의 방법으로 인증되었음을 의미하지는 않습니다 AUTHENTICATION_RESULT_TYPE_BIOMETRIC.
                        Log.d(tag, "onAuthenticationSucceeded RESULT_TYPE_UNKNOWN")
                    }

                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL -> {
                        //사용자가 기기 PIN, 패턴 또는 비밀번호를 입력하여 인증한 경우 보고되는 인증 유형입니다 .
                        Log.d(tag, "onAuthenticationSucceeded RESULT_TYPE_DEVICE_CREDENTIAL")
                    }

                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC -> {
                        //사용자가 생체 인식(예: 지문 또는 얼굴)을 제시하여 인증한 경우에 의해 보고되는 인증 유형입니다 .
                        Log.d(tag, "onAuthenticationSucceeded RESULT_TYPE_BIOMETRIC")
                    }
                }
            }
        }

    override fun getPromptInfo(): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder().setTitle("Biometric login for my app")
            .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG)
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password").build()

    override fun authenticate(cryptoObject: CryptoObject?) {
        if (cryptoObject == null)
            prompt!!.authenticate(promptInfo!!)
        else
            prompt!!.authenticate(promptInfo!!, cryptoObject)
    }
}