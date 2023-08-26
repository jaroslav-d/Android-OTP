package com.example.otp

interface Supervisor {
    fun init(): Pair<Map<String, Any>, List<Map<String, Any>>>
}