package com.example.otussecurityhomework

import android.app.Application
import com.example.otussecurityhomework.data.StorageImpl

class App : Application() {

    val storage by lazy {
        StorageImpl(
            applicationContext = this
        )
    }
}