package com.example.androidotp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OTPActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.fragment_container,
                    MainFragment(),
                    MainFragment::class.java.simpleName
                ).commit()
        }
    }
}