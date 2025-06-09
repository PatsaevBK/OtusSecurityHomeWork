package com.example.otussecurityhomework.data

import android.content.Context
import androidx.core.content.edit
import com.example.otussecurityhomework.data.security.Keys
import com.example.otussecurityhomework.data.security.Security
import com.example.otussecurityhomework.domain.Storage

private const val SHARED_PREFERENCE_NAME = "RSAEncryptedKeysSharedPreferences"
private const val ENCRYPTED_TOKEN = "ENCRYPTED_TOKEN"

private const val BIOMETRIC_ENABLED = "BIOMETRIC_ENABLED"

class StorageImpl(
    private val applicationContext: Context,
) : Storage {

    private val sharedPreferences by lazy {
        applicationContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    private val keys: Keys by lazy {
        Keys(applicationContext, sharedPreferences)
    }

    private val security by lazy {
        Security()
    }

    override fun saveToken(token: String) {
        val secretKey = keys.getAesSecretKey()
        val encryptedToken  = security.encryptAes(token, secretKey)
        sharedPreferences.apply {
            edit {
                putString(ENCRYPTED_TOKEN, encryptedToken)
            }
        }
    }

    override fun getToken(): String? {
        val encryptedToken = sharedPreferences.getString(ENCRYPTED_TOKEN, null) ?: return null
        val secretKey = keys.getAesSecretKey()
        val decryptedToken = security.decryptAes(encryptedToken, secretKey)
        return decryptedToken
    }

    override fun getTokenWithHash(): String? {
        val encryptedToken = sharedPreferences.getString(ENCRYPTED_TOKEN, null) ?: return null
        return buildString {
            appendLine(getToken())
            appendLine("Encrypted hash:")
            appendLine(encryptedToken)
        }
    }

    override fun isLoginBefore(): Boolean {
        val string = sharedPreferences.getString(ENCRYPTED_TOKEN, null)
        return string != null
    }

    override fun changeBiometricEnable(isBiometricEnabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(BIOMETRIC_ENABLED, isBiometricEnabled)
        }
    }

    override fun isBiometricEnable(): Boolean {
        return sharedPreferences.getBoolean(BIOMETRIC_ENABLED, true)
    }
}