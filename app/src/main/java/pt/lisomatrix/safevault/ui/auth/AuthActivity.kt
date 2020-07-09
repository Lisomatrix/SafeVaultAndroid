package pt.lisomatrix.safevault.ui.auth

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.crypto.CryptoProvider
import java.util.concurrent.Executor


class AuthActivity : AppCompatActivity() {

    companion object {
        const val AUTH_OK = "AUTH_OK"
        const val REQUIRE_CRYPTO = "REQUIRE_CRYPTO"
    }

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo.Builder

    private var taskID: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        setSupportActionBar(findViewById(R.id.toolbar))

        val keyguardManager = application.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (keyguardManager.isDeviceSecure) {

            taskID = intent.getLongExtra(REQUIRE_CRYPTO, -1)

            biometricAuthentication()
        }

    }

    private fun biometricAuthentication() {
        executor = ContextCompat.getMainExecutor(applicationContext)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Close application
                    finishAffinity()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val resultIntent = Intent()

                    resultIntent.putExtra(AUTH_OK, true)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Close application
                    finishAffinity()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setDeviceCredentialAllowed(true) // In case device doesn't have biometric sensors
            .setTitle("Authenticate")
            .setSubtitle("Log in order to proceed")
            .setConfirmationRequired(true)

        var cryptoObj: BiometricPrompt.CryptoObject? = null

        if (taskID != -1L) {
            cryptoObj = CryptoProvider.getCryptoObject(taskID)
        }

        if (cryptoObj != null) {
            // For crypto objects password auth is not allowed
            promptInfo
                .setDeviceCredentialAllowed(false)
                .setNegativeButtonText("Credentials only is not allowed")
            biometricPrompt.authenticate(promptInfo.build(), cryptoObj)
        } else {
            biometricPrompt.authenticate(promptInfo.build())
        }
    }
}