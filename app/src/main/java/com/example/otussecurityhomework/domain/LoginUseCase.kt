package com.example.otussecurityhomework.domain

class LoginUseCase(
    private val repository: Repository,
    private val storage: Storage,
) {
    fun login(email: String, password: String): Result<String> {
        val token = repository.login(email, password)
        return if (token != null) {
            storage.saveToken(token)
            Result.success<String>("Success")
        } else Result.failure(IllegalArgumentException("Password or email is not correct"))
    }
}