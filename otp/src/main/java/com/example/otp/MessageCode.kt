package com.example.otp

enum class MessageCode(val what: Int) {
    SEND_CALL(0),
    SEND_CAST(1),
    RECEIVE_CALL(2),
    RECEIVE_CAST(4),
    STOP(8),
    REMOTE(16);
}