package com.example.otussecurityhomework.domain

interface Storage {

    fun saveToken(token: String)

    fun getToken(): String?

    fun getTokenWithHash(): String?
}