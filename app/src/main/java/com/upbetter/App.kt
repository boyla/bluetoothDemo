package com.upbetter

import android.app.Application
import com.inuker.bluetooth.library.BluetoothContext

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        BluetoothContext.set(this)
    }

    companion object {
        private var instance: App? = null
        fun getInstance(): Application? {
            return instance
        }
    }
}