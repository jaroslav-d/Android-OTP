package com.example.otp

import android.os.Bundle

interface SupervisorUtil {
    fun startLink(module: String, args: Bundle)
    fun startChild()
}