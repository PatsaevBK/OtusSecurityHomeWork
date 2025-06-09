package com.example.otussecurityhomework.data.biometric

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.AUTH_BIOMETRIC_STRONG
import android.security.keystore.KeyProperties.BLOCK_MODE_GCM
import android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
import android.security.keystore.KeyProperties.KEY_ALGORITHM_AES
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import java.security.SecureRandom
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal
import androidx.core.content.edit

private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
private const val AUTH_TAG_SIZE = 128
private const val KEY_SIZE = 256
private const val AES_ALGORITHM = "AES"
private const val RSA_MODE_LESS_THAN_M = "RSA/ECB/PKCS1Padding"
private const val RSA_KEY_ALIAS = "RSA_KEY_ALIAS"
private const val RSA_ALGORITHM = "RSA"
private const val BIOMETRIC_PREF_NAME = "biometric"

private const val TOKEN_CYPHER_TEXT = "token_ciphertext"
private const val TOKEN_CYPHER_IV = "token_iv"

private const val TRANSFORMATION = "$KEY_ALGORITHM_AES/" +
        "$BLOCK_MODE_GCM/" +
        "$ENCRYPTION_PADDING_NONE"

class BiometricCipher(
    private val applicationContext: Context,
) {
    private val KEY_ALIAS by lazy { "${applicationContext.packageName}.biometricKey" }

    private val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    private val sharedPreferences by lazy {
        applicationContext.getSharedPreferences(BIOMETRIC_PREF_NAME, Context.MODE_PRIVATE)
    }

    fun loadEncryptedEntity(): EncryptedEntity? {
        val cypherText = sharedPreferences.getString(TOKEN_CYPHER_TEXT, null) ?: return null
        val cypherIv = sharedPreferences.getString(TOKEN_CYPHER_IV, null) ?: return null
        return EncryptedEntity(
            ciphertext = Base64.decode(cypherText, Base64.NO_WRAP),
            iv = Base64.decode(cypherIv, Base64.NO_WRAP)
        )
    }

    fun isBiometricRegister(): Boolean {
        return sharedPreferences.contains(TOKEN_CYPHER_TEXT) && sharedPreferences.contains(TOKEN_CYPHER_IV)
    }

    fun getEncryptor(): BiometricPrompt.CryptoObject {
        val encryptor = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }

        return BiometricPrompt.CryptoObject(encryptor)
    }

    fun getDecryptor(iv: ByteArray): BiometricPrompt.CryptoObject {
        val decryptor = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(AUTH_TAG_SIZE, iv))
        }
        return BiometricPrompt.CryptoObject(decryptor)
    }

    fun encrypt(plaintext: String, encryptor: Cipher): EncryptedEntity {
        require(plaintext.isNotEmpty()) { "Plaintext cannot be empty" }
        val ciphertext = encryptor.doFinal(plaintext.toByteArray())

        val base64Ciphertext = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        val base64Iv = Base64.encodeToString(encryptor.iv, Base64.NO_WRAP)
        sharedPreferences.edit {
            putString(TOKEN_CYPHER_TEXT, base64Ciphertext)
            putString(TOKEN_CYPHER_IV, base64Iv)
        }

        return EncryptedEntity(
            ciphertext,
            encryptor.iv
        )
    }

    fun decrypt(ciphertext: ByteArray, decryptor: Cipher): String {
        val plaintext = decryptor.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }


    private fun getOrCreateKey(): SecretKey {

        keyStore.getKey(KEY_ALIAS, null)?.let { key ->
            return key as SecretKey
        }

        return generateAesSecretKey()
    }

    private fun generateAesSecretKey(): SecretKey {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getKeyGenerator().generateKey()
        } else {
            generateAndSaveAesSecretKeyLessThanM()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getKeyGenerator() = KeyGenerator.getInstance(KEY_ALGORITHM_AES, KEYSTORE_PROVIDER).apply {
        init(getKeyGenSpec())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getKeyGenSpec() = KeyGenParameterSpec.Builder(KEY_ALIAS, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
        .setBlockModes(BLOCK_MODE_GCM)
        .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
        .setKeySize(KEY_SIZE)
        .setUserAuthenticationRequired(true)
        .apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                setUnlockedDeviceRequired(true)

                val hasStringBox = applicationContext
                    .packageManager
                    .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)

                setIsStrongBoxBacked(hasStringBox)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setUserAuthenticationParameters(0, AUTH_BIOMETRIC_STRONG)
            }
        }.build()

    private fun generateAndSaveAesSecretKeyLessThanM(): SecretKey {
        val key = ByteArray(16)
        SecureRandom().run {
            nextBytes(key)
        }
        val encryptedKeyBase64encoded = Base64.encodeToString(
            rsaEncryptKey(key),
            Base64.DEFAULT
        )
        sharedPreferences.edit().apply {
            putString(KEY_ALIAS, encryptedKeyBase64encoded)
            apply()
        }
        return SecretKeySpec(key, AES_ALGORITHM)
    }

    private fun rsaEncryptKey(secret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE_LESS_THAN_M)
        cipher.init(Cipher.ENCRYPT_MODE, getRsaPublicKey())
        return cipher.doFinal(secret)
    }

    private fun getRsaPublicKey(): PublicKey {
        return keyStore.getCertificate(RSA_KEY_ALIAS)?.publicKey ?: generateRsaSecretKey().public
    }

    private fun generateRsaSecretKey(): KeyPair {
        val spec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                PURPOSE_ENCRYPT or PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setUserAuthenticationRequired(true)
                .setRandomizedEncryptionRequired(false)
                .build()
        } else {
            val start: Calendar = Calendar.getInstance()
            val end: Calendar = Calendar.getInstance()
            end.add(Calendar.YEAR, 30)
            KeyPairGeneratorSpec.Builder(applicationContext)
                .setAlias(RSA_KEY_ALIAS)
                .setSubject(X500Principal("CN=${RSA_KEY_ALIAS}"))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build()
        }
        return KeyPairGenerator.getInstance(RSA_ALGORITHM,
            KEYSTORE_PROVIDER
        ).run {
            initialize(spec)
            generateKeyPair()
        }
    }
}

data class EncryptedEntity(
    val ciphertext: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedEntity

        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}