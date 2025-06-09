package com.example.otussecurityhomework.presentation

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otussecurityhomework.data.biometric.BiometricHelper
import com.example.otussecurityhomework.domain.BiometricEnabledUseCase
import com.example.otussecurityhomework.domain.BiometricRegisterUseCase
import com.example.otussecurityhomework.domain.LoginBeforeUseCase
import com.example.otussecurityhomework.domain.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginScreenViewModel(
    loginBeforeUseCase: LoginBeforeUseCase,
    biometricRegisterUseCase: BiometricRegisterUseCase,
    biometricEnabledUseCase: BiometricEnabledUseCase,
    private val biometricHelper: BiometricHelper,
    private val loginUseCase: LoginUseCase,
    private val onSuccess: (String) -> Unit,
    private val onError: (msg: String) -> Unit,
): ViewModel() {

    private val _state = MutableStateFlow(
        SignInScreenState(
            isLoginBefore = loginBeforeUseCase.isLoginBefore(),
            isBiometricAvailable = checkIsBiometricAvailable(),
            isRegisterBiometric = biometricRegisterUseCase.isBiometricRegister(),
            isBiometricEnable = biometricEnabledUseCase.isBiometricEnabled(),
        )
    )
    val state: StateFlow<SignInScreenState> = _state.asStateFlow()

    val emailField = mutableStateOf(TextFieldValue())
    var passwordFieldState = TextFieldState()

    fun changeLoginField(text: TextFieldValue) {
        emailField.value = text
    }

    fun signIn() {
        val result = loginUseCase.login(emailField.value.text, passwordFieldState.text.toString())
        result.onSuccess { onSuccess.invoke(it) }
        result.onFailure { onError.invoke(it.message.toString()) }
    }

    fun registerUserBiometric(
        context: AppCompatActivity,
    ) {
        Log.d("XX", "registerUserBiometric")
        viewModelScope.launch {
            with(biometricHelper) {
                when {
                    isStrongBiometricAvailable() -> registerUserStrongBiometric(context, onSuccess)
                    isWeakBiometricAvailable() -> registerUserWeakBiometric(context, onSuccess)
                }
            }
        }
    }

    fun authenticateBiometricUser(
        context: AppCompatActivity,
    ) {
        Log.d("XX", "authenticateBiometricUser")
        viewModelScope.launch {
            with(biometricHelper) {
                when {
                    isStrongBiometricAvailable() -> authenticateStrongBiometricUser(context, onSuccess)
                    isWeakBiometricAvailable() -> authenticateUserWeakBiometric(context, onSuccess)
                }
            }
        }
    }

    private fun checkIsBiometricAvailable(): Boolean {
        return with(biometricHelper) {
            isStrongBiometricAvailable() || isWeakBiometricAvailable()
        }
    }
}