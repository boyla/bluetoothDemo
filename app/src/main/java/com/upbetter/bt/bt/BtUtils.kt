package com.upbetter.bt.bt

import android.content.Context
import android.util.Log
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.beacon.Beacon
import com.inuker.bluetooth.library.search.SearchRequest
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.upbetter.bt.data.BtDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.String

const val STATE_IDLE = 0
const val STATE_LOADING = 1
const val STATE_ERROR = -1

object BtUtils {

    const val TAG = "BtUtils"

    lateinit var mClient: BluetoothClient
    var mLoadState = STATE_IDLE

    suspend fun scan(
        appCtx: Context,
        ret: MutableStateFlow<MutableList<BtDevice>>
    ) {
        //permissions

        // scan devices
        val request = SearchRequest.Builder()
            .searchBluetoothLeDevice(3000, 3) // 先扫BLE设备3次，每次3s
            .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
            .searchBluetoothLeDevice(2000) // 再扫BLE设备2s
            .build()
        if (!this::mClient.isInitialized) {
            mClient = BluetoothClient(appCtx)
        }
        scanSync(request, ret)
    }

    suspend fun scanSync(
        request: SearchRequest,
        ret: MutableStateFlow<MutableList<BtDevice>>
    ) {
        val dataFlow = flow<MutableList<BtDevice>> {
            var li = ret.value
            if (li == null) {
                li = mutableListOf<BtDevice>()
            }
            li.clear()
            ret.emit(li)
            mClient.search(request, object : SearchResponse {
                override fun onSearchStarted() {
                    mLoadState = STATE_LOADING
                }

                override fun onDeviceFounded(device: SearchResult) {
                    val beacon = Beacon(device.scanRecord)
                    Log.d(
                        TAG,
                        String.format(
                            "beacon for %s\n%s",
                            device.address,
                            beacon.toString()
                        )
                    )
                    li.add(BtDevice(device.address, device.name))
                    ret.tryEmit(li)
                }

                override fun onSearchStopped() {
                    mLoadState = STATE_IDLE
                    Log.d(TAG, "onSearchStopped size: ${li.size}")
                }

                override fun onSearchCanceled() {
                    mLoadState = STATE_IDLE
                    Log.d(TAG, "onSearchCanceled size: ${li.size}")
                }
            })
        }
        dataFlow.collect {
            val newLi = mutableListOf<BtDevice>()
            newLi.addAll(it)
            ret.emit(newLi)
            Log.d(TAG, "scan done, size: ${it.size}")
        }
    }
}
