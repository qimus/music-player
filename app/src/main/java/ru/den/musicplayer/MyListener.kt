package ru.den.musicplayer

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class MyListener(private val lifecycle: Lifecycle) : LifecycleObserver {
    companion object {
        private const val TAG = "MyListener"
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        Log.d(TAG, "start")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        Log.d(TAG, "stop")
    }
}
