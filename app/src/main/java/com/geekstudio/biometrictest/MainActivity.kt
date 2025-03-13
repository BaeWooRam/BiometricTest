package com.geekstudio.biometrictest

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt

class MainActivity : AppCompatActivity(R.layout.main) {
    private val tag = javaClass.simpleName
    private lateinit var biometric: Biometric
    private val testChallenge = "d89s7f89sd7f9s8df"
    private lateinit var etKey: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        biometric = object : BiometricImp() {
            override fun getBiometricPromptAuthenticationCallback(): BiometricPrompt.AuthenticationCallback {
                return object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Log.d(
                            tag,
                            "onAuthenticationSucceeded cryptoObject = ${result.cryptoObject}"
                        )
                        Log.d(
                            tag,
                            "onAuthenticationSucceeded signature = ${result.cryptoObject?.signature}"
                        )
                        Log.d(
                            tag,
                            "onAuthenticationSucceeded cipher = ${result.cryptoObject?.cipher}"
                        )
                        Log.d(tag, "onAuthenticationSucceeded mac = ${result.cryptoObject?.mac}")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Log.d(
                                tag,
                                "onAuthenticationSucceeded identityCredential = ${result.cryptoObject?.identityCredential}"
                            )
                        }
                        val signedData = SignatureHelper.signData(testChallenge)
                        Log.d(tag, "onAuthenticationSucceeded signedData = $signedData")
                        val verifyData = SignatureHelper.verifyData(testChallenge, signedData!!)
                        Log.d(tag, "onAuthenticationSucceeded verifyData = $verifyData")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.d(tag, "onAuthenticationFailed")
                    }
                }
            }

            override fun getPromptInfo(): BiometricPrompt.PromptInfo {
                return BiometricPrompt.PromptInfo.Builder()
                    .setTitle("생체 인증 필요")
                    .setSubtitle("앱을 사용하려면 생체 인증을 해주세요.")
                    .setNegativeButtonText("취소")
                    .build()
            }
        }.apply {
            setUp(this@MainActivity)
        }

        etKey = findViewById<EditText>(R.id.et_key).apply {
            setText(KeystoreHelper.KEY_ALIAS)
        }
        findViewById<Button>(R.id.bt_authenticate).setOnClickListener {
            val cryptoObject = SignatureHelper.getCryptoObject()
            Log.d(tag, "cryptoObject = $cryptoObject")
            Log.d(tag, "cryptoObject mac = ${cryptoObject.mac}")
            Log.d(tag, "cryptoObject cipher = ${cryptoObject.cipher}")
            Log.d(tag, "cryptoObject signature = ${cryptoObject.signature}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Log.d(
                    tag,
                    "cryptoObject identityCredential = ${cryptoObject.identityCredential}"
                )
            }
            biometric.authenticate(cryptoObject)
        }
        findViewById<Button>(R.id.bt_init_signature).setOnClickListener {
            SignatureHelper.initSign()
        }
        findViewById<Button>(R.id.bt_generate_key).setOnClickListener {
            KeystoreHelper.generateKeyPair(etKey.text.toString())
        }
        findViewById<Button>(R.id.bt_check_key).setOnClickListener {
            val keyPair = KeystoreHelper.getKeyPair(etKey.text.toString())
            Log.d(tag, "keyPair private = ${keyPair?.private}")
            Log.d(tag, "keyPair public = ${keyPair?.public}")
        }
        findViewById<Button>(R.id.bt_key_valid).setOnClickListener {
            val isKeyValid = KeystoreHelper.isKeyValid(etKey.text.toString())
            Log.d(tag, "isKeyValid = $isKeyValid")
        }
        findViewById<Button>(R.id.bt_key_accessible).setOnClickListener {
            val isKeyAccessible = KeystoreHelper.isKeyAccessible(etKey.text.toString())
            Log.d(tag, "isKeyAccessible = $isKeyAccessible")
        }
        findViewById<Button>(R.id.bt_key_usable).setOnClickListener {
            val isKeyUsable = KeystoreHelper.isKeyUsable(etKey.text.toString())
            Log.d(tag, "isKeyUsable = $isKeyUsable")
        }
    }
}