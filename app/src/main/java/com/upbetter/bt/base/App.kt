package com.upbetter.bt.base

import android.app.Application
import android.content.Context

class App: Application() {

    lateinit var ctx:Context
    override fun onCreate() {
        super.onCreate()
        ctx = this
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
}