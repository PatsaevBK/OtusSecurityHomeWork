package com.example.otussecurityhomework.presentation

data class SignInScreenState(
    val isLoginBefore: Boolean,
    val isBiometricAvailable: Boolean,
    val isRegisterBiometric: Boolean,
    val isBiometricEnable: Boolean,
)
