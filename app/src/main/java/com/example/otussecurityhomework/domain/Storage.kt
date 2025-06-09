package com.example.otussecurityhomework.domain

interface Storage {

    fun saveToken(token: String)

    fun getToken(): String?

    fun getTokenWithHash(): String?

    fun isLoginBefore(): Boolean

    fun changeBiometricEnable(isBiometricEnabled: Boolean)

    fun isBiometricEnable(): Boolean
}