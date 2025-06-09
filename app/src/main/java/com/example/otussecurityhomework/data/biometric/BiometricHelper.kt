package com.example.otussecurityhomework.data.biometric

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.auth.AuthPromptErrorException
import androidx.biometric.auth.AuthPromptFailureException
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.biometric.auth.authenticate
import com.example.otussecurityhomework.R
import java.util.UUID

class BiometricHelper(
    context: Context
) {
    private val biometricManager = BiometricManager.from(context)
    private val biometricCipher = BiometricCipher(context)

    fun isAnyBiometricRegister(): Boolean = biometricCipher.isBiometricRegister()

    fun isWeakBiometricAvailable(): Boolean {
        return when (biometricManager.canAuthenticate(BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> {
                Log.e("TAG", "Weak Biometric authentication not available")
                false
            }
        }
    }

    suspend fun registerUserWeakBiometric(
        context: AppCompatActivity,
        onSuccess: (String) -> Unit = { },
    ) {
        try {
            val authPrompt = getWeakBiometricPrompt(context)
            val authResult = authPrompt.authenticate(AuthPromptHost(context))
            authResult.cryptoObject?.cipher?.let { cipher ->
                val token = UUID.randomUUID().toString()
                onSuccess.invoke(token)
            }
        } catch (e: AuthPromptErrorException) {
            Log.e("AuthPromptError", e.message ?: "no message")
        } catch (e: AuthPromptFailureException) {
            Log.e("AuthPromptFailure", e.message ?: "no message")
        }
    }

    suspend fun authenticateUserWeakBiometric(
        context: AppCompatActivity,
        onSuccess: (String) -> Unit = { },
    ) {
        try {
            val authPrompt = getWeakBiometricPrompt(context)
            val authResult = authPrompt.authenticate(AuthPromptHost(context))
            authResult.cryptoObject?.cipher?.let { cipher ->
                val token = UUID.randomUUID().toString()
                onSuccess.invoke(token)
            }
        } catch (e: AuthPromptErrorException) {
            Log.e("AuthPromptError", e.message ?: "no message")
        } catch (e: AuthPromptFailureException) {
            Log.e("AuthPromptFailure", e.message ?: "no message")
        }
    }

    private fun getWeakBiometricPrompt(context: Context): Class2BiometricAuthPrompt {
        val weakBiometricPrompt =
            Class2BiometricAuthPrompt.Builder(
                context.getString(R.string.biometric_prompt_title_text),
                context.getString(R.string.biometric_prompt_use_password_instead_text)
            ).apply {
                setSubtitle(context.getString(R.string.biometric_prompt_subtitle_text))
                setDescription(context.getString(R.string.biometric_prompt_description_text))
                setConfirmationRequired(true)
            }.build()
        return weakBiometricPrompt
    }

    fun isStrongBiometricAvailable(): Boolean {
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> {
                Log.e("TAG", "Strong Biometric authentication not available")
                false
            }
        }
    }


    // через Class3BiometricAuthPrompt
    suspend fun registerUserStrongBiometric(
        context: AppCompatActivity,
        onSuccess: (String) -> Unit = {}
    ) {
        try {
            val encryptor = biometricCipher.getEncryptor()
            val authPrompt = getStrongBiometricPrompt(context)
            val authResult = authPrompt.authenticate(AuthPromptHost(context), encryptor)
            authResult.cryptoObject?.cipher?.let { cipher ->
                val token = UUID.randomUUID().toString()
                biometricCipher.encrypt(token, cipher)
                onSuccess.invoke(token)
            }
        } catch (e: AuthPromptErrorException) {
            Log.e("AuthPromptError", e.message ?: "no message")
        } catch (e: AuthPromptFailureException) {
            Log.e("AuthPromptFailure", e.message ?: "no message")
        }
    }

    suspend fun authenticateStrongBiometricUser(context: AppCompatActivity, onSuccess: (token: String) -> Unit) {
        val encryptedData = biometricCipher.loadEncryptedEntity()

        try {
            encryptedData?.let { data ->
                val decryptor = biometricCipher.getDecryptor(encryptedData.iv)
                val authPrompt = getStrongBiometricPrompt(context)
                val authResult = authPrompt.authenticate(AuthPromptHost(context), decryptor)
                authResult.cryptoObject?.cipher?.let { cipher ->
                    val token = biometricCipher.decrypt(encryptedData.ciphertext, cipher)
                    onSuccess.invoke(token)
                }
            }
        } catch (e: AuthPromptErrorException) {
            Log.e("AuthPromptError", e.message ?: "no message")
        } catch (e: AuthPromptFailureException) {
            Log.e("AuthPromptFailure", e.message ?: "no message")
        }
    }

    private fun getStrongBiometricPrompt(context: Context): Class3BiometricAuthPrompt {
        val strongBiometricPrompt =
            Class3BiometricAuthPrompt.Builder(
                context.getString(R.string.biometric_prompt_title_text),
                context.getString(R.string.biometric_prompt_use_password_instead_text)
            ).apply {
                setSubtitle(context.getString(R.string.biometric_prompt_subtitle_text))
                setDescription(context.getString(R.string.biometric_prompt_description_text))
                setConfirmationRequired(true)
            }.build()
        return strongBiometricPrompt
    }
}