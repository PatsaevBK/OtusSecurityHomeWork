package com.example.otussecurityhomework.domain

class BiometricEnabledUseCase(
    private val storage: Storage
) {
    fun changeBiometricEnabled(isBiometricEnable: Boolean) {
        storage.changeBiometricEnable(isBiometricEnable)
    }

    fun isBiometricEnabled(): Boolean {
        return storage.isBiometricEnable()
    }
}