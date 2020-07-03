package pt.lisomatrix.safevault.ui.auth

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import pt.lisomatrix.safevault.R
import java.util.concurrent.Executor


class AuthActivity : AppCompatActivity() {

    companion object {
        const val AUTH_OK = "AUTH_OK"
    }

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        setSupportActionBar(findViewById(R.id.toolbar))

        val keyguardManager = application.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (keyguardManager.isDeviceSecure) {
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
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}