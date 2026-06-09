package com.example.videotranslator

import android.util.Log

object AppLogger {
    private const val TAG = "AI_TRANSLATOR_LOG"

    fun debug(message: String) {
        Log.d(TAG, "[DEBUG] 🟢 $message")
    }

    fun info(message: String) {
        Log.i(TAG, "[INFO]  🔵 $message")
    }

    fun error(message: String, throwable: Throwable? = null) {
        Log.e(TAG, "[ERROR] 🔴 $message", throwable)
    }
}
