package com.upbetter.bt.bt

import android.content.Context
import com.upbetter.bt.data.BtDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BtRepo {
    suspend fun getDevices(ctx: Context): List<BtDevice> {
        return withContext(Dispatchers.Default) {
            val ret = mutableListOf<BtDevice>()
            // scan devices

            ret
        }
    }
}