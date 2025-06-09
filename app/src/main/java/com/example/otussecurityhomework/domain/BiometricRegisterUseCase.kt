package com.example.otussecurityhomework.domain

import com.example.otussecurityhomework.data.biometric.BiometricHelper

class BiometricRegisterUseCase(
    private val biometricHelper: BiometricHelper
) {
    fun isBiometricRegister() = biometricHelper.isAnyBiometricRegister()
}