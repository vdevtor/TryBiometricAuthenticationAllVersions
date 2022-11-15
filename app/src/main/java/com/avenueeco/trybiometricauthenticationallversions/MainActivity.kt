package com.avenueeco.trybiometricauthenticationallversions


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.avenueeco.trybiometricauthenticationallversions.databinding.ActivityMainBinding
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var biometricInfo: BiometricPrompt.PromptInfo

    private val registerBiometricAuthenticator =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkDeviceHasBiometric()
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkDeviceHasBiometric()

        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                startActivity(Intent(this@MainActivity, ActivitySecret::class.java))
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                notifyUser("Authentication error : $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                notifyUser("Authentication Failed")
            }
        })


        biometricInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Some title")
            .setDescription("This app uses biometric authentication to keep your data safe")
            .setSubtitle("Some subtitle")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or  BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()

        binding.btnAuthenticate.setOnClickListener {
            biometricPrompt.authenticate(biometricInfo)
        }
    }

    private fun checkDeviceHasBiometric() {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL or BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                notifyUser("App can authenticate ")
                binding.btnAuthenticate.isEnabled = true
            }


            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                openBiometricSettings()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {

                notifyUser("biometric feature are unavailable")
                binding.btnAuthenticate.isEnabled = false
            }

            else -> openBiometricSettings()
        }
    }


    private fun openBiometricSettings() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            val enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            registerBiometricAuthenticator.launch(enrollIntent)
        } else {
            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            }
            registerBiometricAuthenticator.launch(enrollIntent)
        }

    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}