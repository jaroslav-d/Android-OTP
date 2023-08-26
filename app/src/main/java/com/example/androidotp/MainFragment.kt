package com.example.androidotp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.android.genServer
import com.example.android.linkAs
import com.example.android.unlink
import com.example.androidotp.databinding.FragmentMainBinding
import com.example.otp.GenServer

class MainFragment : Fragment(), GenServer<Int> {

    private val binding by lazy { FragmentMainBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by viewModels()

    private var nameService = MainService::class.simpleName!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireContext()
            .bindService(
                Intent(requireActivity(), MainService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        return binding.root
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(MainFragment::class.qualifiedName, "onServiceConnected: ${name.shortClassName}")
            Messenger(service).linkAs(nameService)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(MainFragment::class.qualifiedName, "onServiceDisconnected: ")
            unlink(nameService)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        genServer.start(this, Bundle())
        genServer.start(viewModel, Bundle())
        binding.root.setOnClickListener {
            Toast.makeText(context, "click root", Toast.LENGTH_SHORT).show()
        }
        binding.next.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container,
                    SecondFragment(),
                    SecondFragment::class.java.simpleName
                ).addToBackStack(MainFragment::class.java.simpleName).commit()
        }
        binding.call.setOnClickListener {
            when(val result = genServer.call("OTPActivity", Bundle())) {
                is Bundle -> {
                    Toast.makeText(
                        context,
                        "is Bundle: ${result.getInt("something")}",
                        Toast.LENGTH_SHORT).show()
                }
                is Binder -> {
                    Toast.makeText(
                        context,
                        "is Binder: ${result.describeContents()}",
                        Toast.LENGTH_SHORT).show()
                }
                is Response -> {
                    Toast.makeText(
                        context,
                        "is Response: ${result.data}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.cast.setOnClickListener {
            genServer.cast("OTPActivity", Bundle())
            genServer.cast(nameService, Bundle())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        genServer.stop(this)
        genServer.stop(viewModel)
    }

    override fun init(args: Bundle): Int {
        return 10
    }

    override fun handleCall(request: Parcelable, from: Messenger, state: Int): Pair<Int, Parcelable> {
        Log.d(this::class.simpleName, "handleCall: state -> $state, request -> $request")
        return Pair(1, Bundle())
    }

    override fun handleCast(request: Parcelable, state: Int): Int {
        Log.d(this::class.simpleName, "handleCast: state -> $state, request -> $request")
        return state - 1
    }

    override fun handleInfo(info: String, state: Int): Int {
        Log.d(this::class.simpleName, "handleInfo: $info")
        return state
    }

    override fun terminate(reason: String, state: Int) {
        Log.d(this::class.simpleName, "terminate: in this place must save all states")
    }

    override fun codeChange(oldVsn: String, state: Int): Int {
        Log.d(this::class.simpleName, "codeChange: i dont know what the action must do in here")
        return state
    }
}