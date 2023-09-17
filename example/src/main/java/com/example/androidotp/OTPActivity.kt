package com.example.androidotp

import android.os.Bundle
import android.os.Messenger
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.example.android.genServer
import com.example.otp.GenServer

class OTPActivity : AppCompatActivity(), GenServer<String> {

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
            genServer.start(this, Bundle())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        genServer.stop(this)
    }

    override fun init(args: Bundle): String {
        return "init"
    }

    override fun handleCall(
        request: Parcelable,
        from: Messenger,
        state: String
    ): Pair<String, Parcelable> {
        return Pair("something with something", Bundle())
    }

    override fun handleCast(request: Parcelable, state: String): String {
        return "something"
    }

    override fun handleInfo(info: String, state: String): String {
        return state
    }

    override fun terminate(reason: String, state: String) {

    }

    override fun codeChange(oldVsn: String, state: String): String {
        return state
    }
}