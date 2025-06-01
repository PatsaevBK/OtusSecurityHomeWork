package com.example.otussecurityhomework.data

import com.example.otussecurityhomework.domain.Repository

private const val FAKE_JWT = "FAKE_JWT"
private const val REQUIRE_EMAIL = "otus@test.com"
private const val REQUIRE_PASSWORD = "otus"

class RepositoryImpl : Repository {
    override fun login(email: String, password: String): String? {
        return if (email == REQUIRE_EMAIL && password == REQUIRE_PASSWORD) FAKE_JWT else null
    }
}