package com.example.androidotp

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import com.example.android.asMessenger
import com.example.android.bindGenServer
import com.example.android.genServer
import com.example.android.getNodeFrom
import com.example.otp.GenServer

class MainService : Service(), GenServer<String> {

    override fun onCreate() {
        super.onCreate()
        Log.d(MainService::class.simpleName, "onCreate: ")
        genServer.start(this, Bundle())
    }

    override fun onBind(intent: Intent): IBinder {
        getNodeFrom(intent)
        return asMessenger().binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(MainService::class.simpleName, "onDestroy: ")
        genServer.stop(this)
    }

    override fun init(args: Bundle): String {
        Log.d(MainService::class.java.simpleName, "init: MainService")
        return "Hello world! and 0"
    }

    override fun handleCall(
        request: Parcelable,
        from: Messenger,
        state: String
    ): Pair<String, Parcelable> {
        val newState =
            state
            .split(" and ")
            .let { "${ it[0] } and ${ it[1].toInt() + 1 }" }
        Log.d(MainService::class.java.simpleName, "handleCall: state -> $newState")
        return Pair(newState, Bundle())
    }

    override fun handleCast(request: Parcelable, state: String): String {
        Log.d(MainService::class.java.simpleName, "handleCast: state -> $state")
        genServer.cast("MainViewModel", Bundle().apply { putByte("number", 12) })
        if (state == "Hello world! and 3") {
            bindGenServer(
                applicationContext,
                Intent(applicationContext, SecondService::class.java),
                SecondService::class.simpleName!!
            )
            Handler(Looper.getMainLooper()).postDelayed(
                { genServer.cast("SecondService", Bundle()) },
                5000
            )
        }
        return state
            .split(" and ")
            .let { "${ it[0] } and ${ it[1].toInt() + 1 }" }
    }

    override fun handleInfo(info: String, state: String): String {
        return state
    }

    override fun terminate(reason: String, state: String) {
        stopSelf()
    }

    override fun codeChange(oldVsn: String, state: String): String {
        return state
    }
}