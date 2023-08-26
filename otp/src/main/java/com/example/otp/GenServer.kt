package com.example.otp

import android.os.Bundle
import android.os.Messenger
import android.os.Parcelable

interface GenServer<State> {
    fun init(args: Bundle): State
    fun handleCall(request: Parcelable, from: Messenger, state: State): Pair<State, Parcelable>
    fun handleCast(request: Parcelable, state: State): State
    fun handleInfo(info: String, state: State): State
    fun terminate(reason: String, state: State)
    fun codeChange(oldVsn: String, state: State): State
}