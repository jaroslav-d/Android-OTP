package com.example.androidotp

import android.app.Application
import android.widget.Toast

class OTPApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Toast.makeText(applicationContext, "im create", Toast.LENGTH_LONG).show()
    }

}