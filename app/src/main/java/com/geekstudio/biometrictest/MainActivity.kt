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
                //오류가 감지되지 않았습니다.
                Toast.makeText(this@MainActivity, "status = BIOMETRIC_SUCCESS", Toast.LENGTH_SHORT).show()

                //개인키 생성
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
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                //생체 인식 하드웨어가 없습니다.
                Toast.makeText(this@MainActivity, "status = BIOMETRIC_ERROR_NO_HARDWARE", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                //하드웨어를 사용할 수 없습니다.
                Toast.makeText(this@MainActivity, "status = BIOMETRIC_ERROR_HW_UNAVAILABLE", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                //사용자에게 등록된 생체 인식이 없습니다.
                Toast.makeText(this@MainActivity, "status = BIOMETRIC_ERROR_NONE_ENROLLED", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                //보안 취약점이 발견되었으며 보안 업데이트로 이 문제가 해결될 때까지 센서를 사용할 수 없습니다.
                Toast.makeText(this@MainActivity, "status = BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED", Toast.LENGTH_SHORT).show()
            }

            else -> Toast.makeText(this@MainActivity, "status = else type", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     *  생체 인증 팝업 초기화
     *  @param[name] 설명1
     *  @param[name2] 설명2
     *  @return 리턴1
     *  @throws ThrowsClass1
     *  @sample identifier initBiometricPopUp() 그대로 사용하면 됨
     *  @author 배우람
     */
    fun initBiometricPopUp() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                /**
                 * 복구할 수 없는 오류가 발생하여 인증이 중지된 경우 호출됩니다.
                 */
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

                /**
                 * 생체인식(예: 지문, 얼굴 등)이 제시되었으나 사용자의 것으로 인식되지 않는 경우 호출됩니다.
                 */
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    //기기 사용자 인증 정보와 생체 인식 사용자 인증 정보 중 어떤 정보가 사용되었는지 확인할 수 있습니다.
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

                /**
                 * 생체 인식(예: 지문, 얼굴 등)이 인식되면 호출되어 사용자가 성공적으로 인증되었음을 나타냅니다.
                 */
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

    /**
     *  생체 인증 3단계 요청
     *  @param[name10] 설명1
     *  @throws ThrowsClass2
     *  @sample showBiometric() 그대로 사용하면 됨
     *  @author 배우람2
     */
    fun showBiometric(){
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

    /**
     *
     */
    private fun getSecretKey(): SecretKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        Log.d(TAG, "keyStore = $keyStore")
        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        Log.d(TAG, "size = ${keyStore.size()}")
        return keyStore.getKey(KEY_NAME, null) as? SecretKey
    }

    /**
     *
     */
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