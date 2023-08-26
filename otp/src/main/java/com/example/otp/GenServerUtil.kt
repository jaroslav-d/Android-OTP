package com.example.otp

import android.os.Bundle
import android.os.Messenger
import android.os.Parcelable

interface GenServerUtil {
    fun <State> start(module: GenServer<State>, args: Bundle): Messenger
    fun <State> start(serverName: String, module: GenServer<State>, args: Bundle): Messenger
    fun call(whom: String, request: Parcelable): Parcelable
    fun cast(whom: String, request: Parcelable)
    fun <State> stop(module: GenServer<State>)
    fun stop(serverName: String)
}