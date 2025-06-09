package com.example.otussecurityhomework.domain

class LoginBeforeUseCase(
    private val storage: Storage,
) {

    fun isLoginBefore() = storage.isLoginBefore()
}