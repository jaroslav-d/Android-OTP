package com.example.android

import android.os.*
import com.example.otp.*

//private var listServices = mutableMapOf<String, Messenger>()

//fun supervisor(callback: Supervisor) = object : SupervisorUtil {
//    override fun <Args> startLink(module: String, args: Args) {
//        callback.init()
//    }
//}
//
//fun <State> genServer(callback: GenServer<State>) = object : GenServerUtil {
//    private var state: State? = null
//    private var callMsg: Parcelable = Bundle()
//    private var handler = Handler(Looper.getMainLooper())
//    private var handlerThread = HandlerThread(callback.javaClass.simpleName)
//
//    private val module = callback.javaClass.simpleName
//    private val lock = Object()
//
//    private fun createThread(module: String) = HandlerThread(module).apply { start() }
//
//    private fun createHandler(handlerThread: HandlerThread) = Handler(handlerThread.looper) {
//        if (it.what == MessageCode.SEND_CAST.what) {
//            state = callback.handleCast(it.obj as Parcelable, state!!)
//        }
//        if (it.what == MessageCode.RECEIVE_CAST.what) { }
//        if (it.what == MessageCode.SEND_CALL.what) {
//            val (newState, response) = callback.handleCall(it.obj as Parcelable, it.replyTo, state!!)
//            state = newState
//            val message = Message.obtain().apply {
//                what = MessageCode.RECEIVE_CALL.what
//                obj = response
//            }
//            it.replyTo.send(message)
//        }
//        if (it.what == MessageCode.RECEIVE_CALL.what) {
//            callMsg = it.obj as Parcelable
//            synchronized(lock) {
//                lock.notify()
//            }
//        }
//        return@Handler true
//    }
//
//    override fun start(args: Bundle) {
//        if (listServices[module] != null) return
//        handlerThread = createThread(module)
//        handler = createHandler(handlerThread)
//        handler.post { state = callback.init(args) }
//        listServices[module] = Messenger(handler)
//    }
//
//    override fun call(whom: String, request: Parcelable): Parcelable {
//        val messenger = listServices[whom] ?: return Bundle()
//        val message = Message.obtain().apply {
//            what = MessageCode.SEND_CALL.what
//            obj = request
//            replyTo = Messenger(handler)
//        }
//        messenger.send(message)
//        synchronized(lock) {
//            lock.wait()
//        }
//        return callMsg
//    }
//
//    override fun cast(whom: String, request: Parcelable) {
//        val messenger = listServices[whom] ?: return
//        val message = Message.obtain().apply {
//            what = MessageCode.SEND_CAST.what
//            obj = request
//        }
//        messenger.send(message)
//    }
//
//    override fun stop() {
//        if (listServices[module] == null) return
//        if (module == handlerThread.name) { handlerThread.quit() }
//        listServices.remove(module)
//    }
//}


private interface GenServerInstance {
    val name: String
    val messenger: Messenger
}

private val listServices = mutableMapOf<String, GenServerInstance>()

class AlreadyStartGenServerException : Exception("the gen_server is already started")
class NotFoundGenServerException : Exception("the gen_server not found in system")

val genServer = object : GenServerUtil {

    val mHandlerThread = HandlerThread("gen_server_call").apply { start() }

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
        val service = listServices[whom] ?: return Bundle()
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
        service.messenger.send(message)
        synchronized(lock) {
            lock.wait()
        }
        return callMsg
    }

    override fun cast(whom: String, request: Parcelable) {
        val service = listServices[whom] ?: return
        val message = Message.obtain().apply {
            what = MessageCode.SEND_CAST.what
            obj = request
        }
        service.messenger.send(message)
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

fun Messenger.unlink() {
    val instance = listServices.entries.find { it.value.messenger == this } ?: return
    listServices.remove(instance.key)
}

fun unlink(moduleName: String) {
    listServices.remove(moduleName)
}

fun <State> GenServer<State>.asMessenger(): Messenger {
    val service = listServices[javaClass.simpleName] ?: throw NotFoundGenServerException()
    return service.messenger
}

fun findMessenger(serverName: String): Messenger {
    val service = listServices[serverName] ?: throw NotFoundGenServerException()
    return service.messenger
}