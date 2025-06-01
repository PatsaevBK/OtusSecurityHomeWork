package com.example.otussecurityhomework

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import com.example.otussecurityhomework.domain.LoginUseCase

class LoginScreenViewModel(
    private val loginUseCase: LoginUseCase,
    private val onSuccess: () -> Unit,
    private val onError: (msg: String) -> Unit,
) {

    val emailField = mutableStateOf(TextFieldValue())
    var passwordFieldState = TextFieldState()

    fun changeLoginField(text: TextFieldValue) {
        emailField.value = text
    }

    fun signIn() {
        val result = loginUseCase.login(emailField.value.text, passwordFieldState.text.toString())
        result.onSuccess { onSuccess.invoke() }
        result.onFailure { onError.invoke(it.message.toString()) }
    }
}