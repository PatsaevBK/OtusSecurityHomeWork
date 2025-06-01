package com.example.otussecurityhomework.domain

interface Repository {
    fun login(email: String, password: String): String?
}