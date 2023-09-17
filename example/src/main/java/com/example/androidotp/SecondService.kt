package com.example.androidotp

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import android.os.Parcelable
import android.util.Log
import com.example.android.asMessenger
import com.example.android.genServer
import com.example.android.getNodeFrom
import com.example.otp.GenServer

class SecondService : Service(), GenServer<SecondService.State> {

    enum class State(val flag: Int) {
        FIRST(0),
        SECOND(1),
        THIRD(2),
        FOURTH(3)
    }

    override fun onCreate() {
        super.onCreate()
        genServer.start(this, Bundle())
    }

    override fun onBind(intent: Intent): IBinder {
        getNodeFrom(intent)
        return asMessenger().binder
    }

    override fun init(args: Bundle): State {
        Log.d(SecondService::class.simpleName, "init: ")
        return State.FIRST
    }

    override fun handleCall(
        request: Parcelable,
        from: Messenger,
        state: State
    ): Pair<State, Parcelable> {
        Log.d(SecondService::class.simpleName, "handleCall: state -> ${state.name}")
        val newState = State.values().find { it.flag == (state.flag + 1) % State.values().size }!!
        return Pair(newState, request)
    }

    override fun handleCast(request: Parcelable, state: State): State {
        Log.d(SecondService::class.simpleName, "handleCast: state -> ${state.name}")
        genServer.cast("MainViewModel", Bundle())
        val newState = State.values().find { it.flag == (state.flag + 1) % State.values().size }
        return newState ?: State.FIRST
    }

    override fun handleInfo(info: String, state: State): State {
        return state
    }

    override fun terminate(reason: String, state: State) {
        Log.d(SecondService::class.simpleName, "terminate: ууу сука")
    }

    override fun codeChange(oldVsn: String, state: State): State {
        return State.FOURTH
    }
}