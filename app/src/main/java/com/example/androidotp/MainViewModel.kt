package com.example.androidotp

import android.os.Bundle
import android.os.Messenger
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.otp.GenServer

class MainViewModel : ViewModel(), GenServer<Boolean> {


    override fun init(args: Bundle): Boolean {
        return true
    }

    override fun handleCall(
        request: Parcelable,
        from: Messenger,
        state: Boolean
    ): Pair<Boolean, Parcelable> {
        Log.d(this::class.simpleName, "handleCall: state -> $state, request -> $request")
        return Pair(false, Bundle())
    }

    override fun handleCast(request: Parcelable, state: Boolean): Boolean {
        Log.d(this::class.simpleName, "handleCast: state -> $state, request -> $request")
        return state and state
    }

    override fun handleInfo(info: String, state: Boolean): Boolean {
        Log.d(this::class.simpleName, "handleInfo: state -> $state")
        return state xor state
    }

    override fun terminate(reason: String, state: Boolean) {
        Log.d(this::class.simpleName, "terminate: im free like a bird in the sky")
    }

    override fun codeChange(oldVsn: String, state: Boolean): Boolean {
        return state
    }
}