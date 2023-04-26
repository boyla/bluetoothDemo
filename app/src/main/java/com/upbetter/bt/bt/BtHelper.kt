package com.upbetter.bt.bt

import android.content.Context
import android.util.Log
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.Code.REQUEST_SUCCESS
import com.inuker.bluetooth.library.Constants.STATUS_CONNECTED
import com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED
import com.inuker.bluetooth.library.beacon.Beacon
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener
import com.inuker.bluetooth.library.search.SearchRequest
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.upbetter.bt.util.ToastUtil
import java.util.concurrent.CopyOnWriteArrayList


const val STATE_IDLE = 0
const val STATE_LOADING = 1
const val STATE_ERROR = -1

object BtHelper {

    const val TAG = "BtHelper"

    lateinit var mClient: BluetoothClient
    var mLoadState = STATE_IDLE
    var deviceLi = CopyOnWriteArrayList<SearchResult>()
    var currentConnects = mutableListOf<SearchResult>()
    val statusChangeListeners = mutableListOf<() -> Unit>()
    fun scan(appCtx: Context, onListUpdate: () -> Unit) {
        deviceLi.clear()
        onListUpdate()
        val request = SearchRequest.Builder()
//            .searchBluetoothLeDevice(5000) // 先扫BLE设备3次，每次3s
            .searchBluetoothClassicDevice(10000) // 再扫经典蓝牙5s
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
                if (deviceLi.size == 0) {
                    deviceLi.add(found)
                } else {
                    var addIn = false
                    for ((i, e) in deviceLi.withIndex()) {
                        if (found.rssi >= e.rssi) {
                            deviceLi.add(i, found)
                            addIn = true
                            break
                        }
                    }
                    if (!addIn) {
                        deviceLi.add(found)
                    }
                }
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

    fun connect(ret: SearchResult, onSuccess: (() -> Unit)? = null, onFail: (() -> Unit)? = null) {
        if (currentConnects.contains(ret)) {
            ToastUtil.showToast(ToastUtil.ctx!!, "${ret.name} 已连接")
            return
        }
        for (d in currentConnects) {
            disconnect(d)
        }
        val address = ret.address
        val name = ret.name
        mClient.registerConnectStatusListener(address, object : BleConnectStatusListener() {
            override fun onConnectStatusChanged(mac: String?, status: Int) {
                if (status == STATUS_CONNECTED) {
                    if (!currentConnects.contains(ret)) {
                        currentConnects.add(ret)
                    }
                    Log.d(TAG, "onConnectStatusChanged STATUS_CONNECTED ${ret.address}")
                    ToastUtil.showToast(ToastUtil.ctx!!, "$name 连接成功")
                } else if (status == STATUS_DISCONNECTED) {
                    if (currentConnects.contains(ret)) {
                        ToastUtil.showToast(ToastUtil.ctx!!, "$name 连接断开")
                        currentConnects.remove(ret)
                    }
                    Log.d(TAG, "onConnectStatusChanged STATUS_DISCONNECTED ${ret.address}")
                }
                for (lis in statusChangeListeners) {
                    lis.invoke()
                }
            }
        })

        mClient.connect(
            ret.address
        ) { code, profile ->
            if (code == REQUEST_SUCCESS) {
                onSuccess?.invoke()
            } else {
                onFail?.invoke()
            }
        }
    }

    fun registOnStatusChange(onChange: () -> Unit) {
        statusChangeListeners.add(onChange)
    }

    fun unregistOnStatusChange(onChange: () -> Unit) {
        statusChangeListeners.remove(onChange)
    }

//    var updateCurrentInfo: (() -> Unit)? = null

    fun disconnect(ret: SearchResult) {
        mClient.disconnect(ret.address)
    }

    fun test() {
        mClient
    }
}
