package com.upbetter.bt.bt

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.Code.REQUEST_SUCCESS
import com.inuker.bluetooth.library.Constants.BOND_BONDING
import com.inuker.bluetooth.library.Constants.STATUS_CONNECTED
import com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED
import com.inuker.bluetooth.library.beacon.Beacon
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse
import com.inuker.bluetooth.library.model.BleGattProfile
import com.inuker.bluetooth.library.receiver.listener.BluetoothBondListener
import com.inuker.bluetooth.library.search.SearchRequest
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.upbetter.App
import com.upbetter.bt.R
import com.upbetter.bt.util.ToastUtil
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList


const val STATE_IDLE = 0
const val STATE_LOADING = 1
const val STATE_ERROR = -1

object BtHelper {

    const val TAG = "BtHelper"
    var retry = false

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    lateinit var mClient: BluetoothClient
    var mLoadState = STATE_IDLE
    var deviceLi = CopyOnWriteArrayList<SearchResult>()
    var currentConnects = mutableListOf<SearchResult>()
    val statusChangeListeners = mutableListOf<() -> Unit>()
    var callbackDevice: BleGattProfile? = null
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
        if (!mClient.isBluetoothOpened) {
            return
        }
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
        if (ActivityCompat.checkSelfPermission(
                App.getInstance()!!.applicationContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothAdapter.enable()
//        mClient.openBluetooth()
    }

    fun closeBt() {
        mClient.closeBluetooth();
    }

    @SuppressLint("MissingPermission")
    fun connect(
        ret: SearchResult,
        onSuccess: ((device: BleGattProfile) -> Unit)? = null,
        onFail: (() -> Unit)? = null
    ) {
        if (currentConnects.contains(ret)) {
            ToastUtil.showToast(ToastUtil.ctx!!, "${ret.name} 已连接")
            return
        }
        bluetoothAdapter.cancelDiscovery()
        for (d in currentConnects) {
            disconnect(d)
        }
        val address = ret.address
        val name = ret.name
        retry = true
        mClient.registerConnectStatusListener(address, object : BleConnectStatusListener() {
            override fun onConnectStatusChanged(mac: String?, status: Int) {
                if (status == STATUS_CONNECTED) {
                    if (!currentConnects.contains(ret)) {
                        currentConnects.add(ret)
                    }
                    ToastUtil.showToast(ToastUtil.ctx!!, "$name 连接成功")
                    Log.d(TAG, "onConnectStatusChanged STATUS_CONNECTED ${ret.address}")
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
        var conn: (() -> Unit)? = null
        conn = { ->
            mClient.connect(
                ret.address
            ) { code, profile ->
                if (code == REQUEST_SUCCESS) {
                    onSuccess?.invoke(profile)
                    callbackDevice?.services?.let {
                        for (i in it) {
                            Log.d(TAG, "found service: $i")
                        }
                    }
                    val service = callbackDevice!!.services[0]
                    val sid = service.uuid
                    val cid = service.characters[0].uuid
                    Log.d(TAG, "notify sid: $sid, cid: $cid")
                    this@BtHelper.notify(
                        sid,
                        cid,
                        object : BleNotifyResponse {
                            override fun onNotify(
                                service: UUID,
                                character: UUID,
                                value: ByteArray
                            ) {
                                Log.d(TAG, "onNotify service: ${service}, character: $${character}")
                            }

                            override fun onResponse(code: Int) {
                                Log.e(TAG, "notify onResponse $code")
                                if (code == REQUEST_SUCCESS) {
                                }
                            }
                        })
                } else {
                    Log.e(TAG, "connect fail $code")
                    onFail?.invoke()
                    if (retry) {
                        Log.e(TAG, "retry pair")
                        pairOff(ret)
                        pair(ret)
                        retry = false
                    }
                }
            }

        }
        mClient.registerBluetoothBondListener(object : BluetoothBondListener() {
            override fun onBondStateChanged(s: String?, c: Int) {
                //BOND_BONDED = 12 BOND_BONDING = 11 BOND_NONE = 10
                Log.d(TAG, "onBondStateChanged: $s, c: $c")
                if ((BOND_BONDED == c || BOND_BONDING == c) && TextUtils.equals(s, ret.address)) {
                    conn.invoke()
                }
            }
        })
        var isBond = false
        for (d in bluetoothAdapter.bondedDevices) {
            if (TextUtils.equals(d.address, ret.address)) {
                isBond = true
                break
            }
        }
        if (isBond) {
            Log.d(TAG, "already bond: ${ret.address}, connect")
            conn.invoke()
        } else {
            pair(ret)
        }
    }

    fun notify(service: UUID, character: UUID, response: BleNotifyResponse) {
        val d = currentConnects[0]
        mClient.notify(d.address, service, character, response)
    }

    fun notify(mac: String, service: UUID, character: UUID, response: BleNotifyResponse) {
        mClient.notify(mac, service, character, response)
    }

    fun registOnStatusChange(onChange: () -> Unit) {
        statusChangeListeners.add(onChange)
    }

    fun unregistOnStatusChange(onChange: () -> Unit) {
        statusChangeListeners.remove(onChange)
    }

    fun disconnect(ret: SearchResult) {
        mClient.disconnect(ret.address)
    }

    fun test() {
//        bluetoothAdapter.listenUsingRfcommWithServiceRecord()
        mClient
    }

    const val PROFILE_HEADSET = 0
    const val PROFILE_A2DP = 1
    const val PROFILE_OPP = 2
    const val PROFILE_HID = 3
    const val PROFILE_PANU = 4
    const val PROFILE_NAP = 5
    const val PROFILE_A2DP_SINK = 6

    @SuppressLint("MissingPermission")
    fun getDeviceType(d: SearchResult): Int {
        val device = d.device
        if (device == null) {
            Log.d(TAG, "device null")
            return 0
        }
        val bluetoothClass = device.bluetoothClass
//        Log.d(TAG, "getDeviceType ${bluetoothClass?.majorDeviceClass}")
        return if (bluetoothClass == null) {
            R.drawable.ic_unknow
        } else when (bluetoothClass.majorDeviceClass) {
            BluetoothClass.Device.Major.COMPUTER -> R.drawable.ic_laptop
            BluetoothClass.Device.Major.PHONE -> R.drawable.ic_cellphone
            BluetoothClass.Device.Major.PERIPHERAL -> R.drawable.ic_mouse
            BluetoothClass.Device.Major.WEARABLE -> R.drawable.ic_watch
            BluetoothClass.Device.Major.TOY -> R.drawable.ic_toy
            BluetoothClass.Device.Major.IMAGING -> R.drawable.ic_image
            else -> if (doesClassMatch(
                    bluetoothClass,
                    PROFILE_HEADSET
                )
            ) R.drawable.ic_headset else if (doesClassMatch(bluetoothClass, PROFILE_A2DP)) {
                R.drawable.ic_headset
            } else {
                R.drawable.ic_unknow
            }
        }
    }

    fun doesClassMatch(bluetoothClass: BluetoothClass, profile: Int): Boolean {
        return if (profile == PROFILE_A2DP) {
            if (bluetoothClass.hasService(BluetoothClass.Service.RENDER)) {
                return true
            }
            when (bluetoothClass.deviceClass) {
                BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO, BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES, BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER, BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> true
                else -> false
            }
        } else if (profile == PROFILE_A2DP_SINK) {
            if (bluetoothClass.hasService(BluetoothClass.Service.CAPTURE)) {
                return true
            }
            when (bluetoothClass.deviceClass) {
                BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO, BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX, BluetoothClass.Device.AUDIO_VIDEO_VCR -> true
                else -> false
            }
        } else if (profile == PROFILE_HEADSET) {
            // The render service class is required by the spec for HFP, so is a
            // pretty good signal
            if (bluetoothClass.hasService(BluetoothClass.Service.RENDER)) {
                return true
            }
            when (bluetoothClass.deviceClass) {
                BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE, BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET, BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> true
                else -> false
            }
        } else if (profile == PROFILE_OPP) {
            if (bluetoothClass.hasService(BluetoothClass.Service.OBJECT_TRANSFER)) {
                return true
            }
            when (bluetoothClass.deviceClass) {
                BluetoothClass.Device.COMPUTER_UNCATEGORIZED, BluetoothClass.Device.COMPUTER_DESKTOP, BluetoothClass.Device.COMPUTER_SERVER, BluetoothClass.Device.COMPUTER_LAPTOP, BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA, BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA, BluetoothClass.Device.COMPUTER_WEARABLE, BluetoothClass.Device.PHONE_UNCATEGORIZED, BluetoothClass.Device.PHONE_CELLULAR, BluetoothClass.Device.PHONE_CORDLESS, BluetoothClass.Device.PHONE_SMART, BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY, BluetoothClass.Device.PHONE_ISDN -> true
                else -> false
            }
        } else if (profile == PROFILE_HID) {
            bluetoothClass.deviceClass and BluetoothClass.Device.Major.PERIPHERAL == BluetoothClass.Device.Major.PERIPHERAL
        } else if (profile == PROFILE_PANU || profile == PROFILE_NAP) {
            // No good way to distinguish between the two, based on class bits.
            if (bluetoothClass.hasService(BluetoothClass.Service.NETWORKING)) {
                true
            } else bluetoothClass.deviceClass and BluetoothClass.Device.Major.NETWORKING == BluetoothClass.Device.Major.NETWORKING
        } else {
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun pair(ret: SearchResult) {
        try {
//            val method = BluetoothDevice::class.java.getMethod("createBond")
//            method.invoke(ret.device)
            val state = ret.device.bondState
            Log.d(TAG, "pairOn state: $state")
            val createSuccess = ret.device.createBond()
            Log.d(TAG, "pairOn createBond Success: $createSuccess")
        } catch (e: Exception) {
            Log.d(TAG, "pairOn Exception: ${e.message}")
        }
    }

    fun pairOff(ret: SearchResult) {
        try {
            val method = BluetoothDevice::class.java.getMethod("removeBond")
            method.invoke(ret.device)
        } catch (e: Exception) {
            Log.d(TAG, "pairOff Exception: ${e.message}")
        }
    }

}
