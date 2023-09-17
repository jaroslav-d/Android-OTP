package com.example.android

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import com.example.otp.*

private interface GenServerInstance {
    val name: String
    val messenger: Messenger
}

private interface NodeGenServerInstance : GenServerInstance

private val listServices = mutableMapOf<String, GenServerInstance>()

class AlreadyStartGenServerException : Exception("the gen_server is already started")
class NotFoundGenServerException : Exception("the gen_server not found in system")

val genServer = object : GenServerUtil {

    private val mHandlerThread = HandlerThread("gen_server_call").apply { start() }

    private fun <State> createLink(
        serverName: String,
        callback: GenServer<State>,
        args: Bundle
    ) = object : GenServerInstance {
        private var state = callback.init(args)
        override val name = serverName
        val handlerThread = HandlerThread(name).apply { start() }
        val handler = Handler(handlerThread.looper) {
            if (it.what == MessageCode.SEND_CAST.what) {
                state = callback.handleCast(it.obj as Parcelable, state)
            }
            if (it.what == MessageCode.RECEIVE_CAST.what) { }
            if (it.what == MessageCode.SEND_CALL.what) {
                val (newState, response) = callback.handleCall(it.obj as Parcelable, it.replyTo, state)
                state = newState
                val message = Message.obtain().apply {
                    what = MessageCode.RECEIVE_CALL.what
                    obj = response
                }
                it.replyTo.send(message)
            }
            if (it.what == MessageCode.STOP.what) {
                callback.terminate("normal", state)
                listServices.remove(name)
                handlerThread.quit()
            }
            if (it.what == MessageCode.REMOTE.what) {
                val whom = it.data.getString("whom")
                val message = it.data.getParcelable<Message>("message")
                val except = it.data.getStringArrayList("except")
                if (whom == null || message == null || except == null) {}
                else {
                    send(whom, message, except)
                }
            }
            return@Handler true
        }
        override val messenger get() = Messenger(handler)
    }

    override fun <State> start(module: GenServer<State>, args: Bundle): Messenger {
        val moduleName = module.javaClass.simpleName
        return start(moduleName, module, args)
    }

    override fun <State> start(serverName: String, module: GenServer<State>, args: Bundle): Messenger {
        if (listServices[serverName] != null) throw AlreadyStartGenServerException()
        val link = createLink(serverName, module, args)
        listServices[serverName] = link
        return link.messenger
    }

    override fun call(whom: String, request: Parcelable): Parcelable {
        val lock = Object()
        var callMsg: Parcelable = Bundle()
        val handler = Handler(mHandlerThread.looper) {
            if (it.what == MessageCode.RECEIVE_CALL.what) {
                callMsg = it.obj as Parcelable
                synchronized(lock) {
                    lock.notify()
                }
            }
            return@Handler true
        }
        val message = Message.obtain().apply {
            what = MessageCode.SEND_CALL.what
            obj = request
            replyTo = Messenger(handler)
        }
        send(whom, message)
        synchronized(lock) {
            lock.wait()
        }
        return callMsg
    }

    override fun cast(whom: String, request: Parcelable) {
        val message = Message.obtain().apply {
            what = MessageCode.SEND_CAST.what
            obj = request
        }
        send(whom, message)
    }

    private fun send(whom: String, message: Message, except: List<String> = listOf()) {
        val isSend = listServices[whom]?.messenger?.send(message)
        if (isSend == null) sendRemote(whom, message, except)
    }

    private fun sendRemote(whom: String, message: Message, except: List<String>) {
        val remotes = listServices.values.filterIsInstance<NodeGenServerInstance>()
        val nodeMessage = Message.obtain().apply {
            what = MessageCode.REMOTE.what
            data = Bundle().apply {
                putString("whom", whom)
                putParcelable("message", message)
                putStringArrayList("except", ArrayList(remotes.map { it.name } + except))
            }
        }
        remotes.forEach {
            if (except.contains(it.name)) return@forEach
            it.messenger.send(nodeMessage)
        }
    }

    override fun <State> stop(module: GenServer<State>) {
        val moduleName = module.javaClass.simpleName
        stop(moduleName)
    }

    override fun stop(serverName: String) {
        val service = listServices[serverName] ?: return
        val message = Message.obtain().apply {
            what = MessageCode.STOP.what
        }
        service.messenger.send(message)
    }
}

fun Messenger.linkAs(moduleName: String) {
    if (listServices.entries.find { it.value.messenger == this } != null) return
    listServices[moduleName] = object : GenServerInstance {
        override val name: String = moduleName
        override val messenger: Messenger = this@linkAs
    }
}

fun Messenger.linkAsNode(moduleName: String) {
    if (listServices.entries.find { it.value.messenger == this } != null) return
    listServices[moduleName] = object : NodeGenServerInstance {
        override val name: String = moduleName
        override val messenger: Messenger = this@linkAsNode
    }
}

fun Messenger.unlink() {
    val instance = listServices.entries.find { it.value.messenger == this } ?: return
    listServices.remove(instance.key)
}

fun unlink(moduleName: String) {
    listServices.remove(moduleName)
}

fun <State> GenServer<State>.asMessenger(name: String = javaClass.simpleName): Messenger {
    val service = listServices[name] ?: throw NotFoundGenServerException()
    return service.messenger
}

fun findMessenger(serverName: String): Messenger {
    val service = listServices[serverName] ?: throw NotFoundGenServerException()
    return service.messenger
}

fun <State> GenServer<State>.bindGenServer(
    context: Context,
    intent: Intent,
    nameNode: String,
    thisServerName: String = this.javaClass.simpleName
): ServiceConnection {
    val thisServer = listServices[thisServerName] ?: throw NotFoundGenServerException()
    intent.putExtra("nodeName", thisServer.name)
    intent.putExtra("nodeMessenger", thisServer.messenger)
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            Messenger(service).linkAsNode(nameNode)
        }
        override fun onServiceDisconnected(componentName: ComponentName) {
            unlink(nameNode)
        }
    }
    context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    return serviceConnection
}

fun getNodeFrom(intent: Intent) {
    val nodeName = intent.getStringExtra("nodeName")
    val nodeMessenger = intent.getParcelableExtra<Messenger>("nodeMessenger")
    if (nodeName == null || nodeMessenger == null) {}
    else {
        nodeMessenger.linkAsNode(nodeName)
    }
}