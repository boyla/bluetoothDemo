package com.upbetter.bt.bt

import android.content.Context
import android.util.Log
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.beacon.Beacon
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener
import com.inuker.bluetooth.library.search.SearchRequest
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import java.lang.String

const val STATE_IDLE = 0
const val STATE_LOADING = 1
const val STATE_ERROR = -1

object BtHelper {

    const val TAG = "BtHelper"

    lateinit var mClient: BluetoothClient
    var mLoadState = STATE_IDLE
    var deviceLi = mutableListOf<SearchResult>()
    fun scan(appCtx: Context, onListUpdate: () -> Unit) {
        deviceLi.clear()
        onListUpdate()
        val request = SearchRequest.Builder()
//            .searchBluetoothLeDevice(5000) // 先扫BLE设备3次，每次3s
            .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
//            .searchBluetoothLeDevice(5000)
//            .searchBluetoothClassicDevice(5000)
//            .searchBluetoothLeDevice(2000) // 再扫BLE设备2s
            .build()
        if (!this::mClient.isInitialized) {
            mClient = BluetoothClient(appCtx)
            mClient.registerBluetoothStateListener(object : BluetoothStateListener() {
                override fun onBluetoothStateChanged(isOpen: Boolean) {
                    Log.d(TAG, "onBluetoothStateChanged isOpen: $isOpen")
                }
            })
        }
        openBt()
        mClient.search(request, object : SearchResponse {
            override fun onSearchStarted() {
                Log.d(TAG, "onSearchStarted size: ${deviceLi.size}")
                mLoadState = STATE_LOADING
            }

            override fun onDeviceFounded(found: SearchResult) {
                val beacon = Beacon(found.scanRecord)
                Log.d(
                    TAG,
                    String.format(
                        "beacon for %s\n%s",
                        found.address,
                        beacon.toString()
                    )
                )
                deviceLi.add(found)
                onListUpdate()
            }

            override fun onSearchStopped() {
                mLoadState = STATE_IDLE
                Log.d(TAG, "onSearchStopped size: ${deviceLi.size}")
                onListUpdate()
            }

            override fun onSearchCanceled() {
                mLoadState = STATE_IDLE
                Log.d(TAG, "onSearchCanceled size: ${deviceLi.size}")
            }
        })

    }

    fun openBt() {
        mClient.openBluetooth();
    }

    fun closeBt() {
        mClient.closeBluetooth();
    }
}
