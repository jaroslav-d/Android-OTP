package com.example.androidotp

import android.os.Bundle
import android.os.Messenger
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.otp.GenServer

class MainViewModel : ViewModel(), GenServer<MainViewModel.State> {

    sealed class State {
        class On : State()
        class Off : State()
    }

    override fun init(args: Bundle): State {
        return State.Off()
    }

    override fun handleCall(
        request: Parcelable,
        from: Messenger,
        state: State
    ): Pair<State, Parcelable> {
        Log.d(MainViewModel::class.java.simpleName, "handleCall: state -> ${state.javaClass.simpleName}, request -> $request")
        return Pair(State.On(), Bundle())
    }

    override fun handleCast(request: Parcelable, state: State): State {
        Log.d(MainViewModel::class.java.simpleName, "handleCast: state -> ${state.javaClass.simpleName}, request -> $request")
        return when(state) {
            is State.On -> State.Off()
            is State.Off -> State.On()
        }
    }

    override fun handleInfo(info: String, state: State): State {
        Log.d(MainViewModel::class.java.simpleName, "handleInfo: state -> $state")
        return state
    }

    override fun terminate(reason: String, state: State) {
        Log.d(MainViewModel::class.java.simpleName, "terminate: im free like a bird in the sky")
    }

    override fun codeChange(oldVsn: String, state: State): State {
        return state
    }
}