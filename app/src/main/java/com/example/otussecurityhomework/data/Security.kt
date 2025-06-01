package com.example.otussecurityhomework.data

import android.util.Base64
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
private const val IV = "3134003223491201"
private const val GCM_IV_LENGTH = 12

class Security {

    private val FIXED_IV = IV.toByteArray()

    fun encryptAes(plainText: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, getInitializationVector())
        val encodedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decryptAes(encrypted: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, getInitializationVector())
        val decodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decoded = cipher.doFinal(decodedBytes)
        return String(decoded, Charsets.UTF_8)
    }

    private fun getInitializationVector(): AlgorithmParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        FIXED_IV.copyInto(destination = iv, destinationOffset = 0, startIndex = 0, endIndex = GCM_IV_LENGTH)
        return GCMParameterSpec(128, iv)
    }
}