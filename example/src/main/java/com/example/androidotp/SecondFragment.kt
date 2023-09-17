package com.example.androidotp

import android.os.Bundle
import android.os.Messenger
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.android.genServer
import com.example.androidotp.databinding.FragmentSecondBinding
import com.example.otp.GenServer

class SecondFragment : Fragment(), GenServer<Int> {

    private val binding: FragmentSecondBinding by lazy { FragmentSecondBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.prev.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.root.setOnClickListener {
            genServer.cast("MainService", Bundle().apply { putByte("nunmber", 1) })
        }
    }

    override fun init(args: Bundle): Int = 10


    override fun handleCall(
        request: Parcelable,
        from: Messenger,
        state: Int
    ): Pair<Int, Parcelable> {
        return Pair(state + 1, request)
    }

    override fun handleCast(request: Parcelable, state: Int): Int {
        return state + 2
    }

    override fun handleInfo(info: String, state: Int): Int {
        return state
    }

    override fun terminate(reason: String, state: Int) {
        Log.d(this::class.simpleName, "terminate: $state")
    }

    override fun codeChange(oldVsn: String, state: Int): Int {
        Log.d(this::class.simpleName, "codeChange: $state")
        return state
    }
}