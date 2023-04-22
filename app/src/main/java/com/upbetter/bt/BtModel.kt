package com.upbetter.bt

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.upbetter.bt.bt.BtUtils
import com.upbetter.bt.data.BtDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BtModel(private val app: Application) : AndroidViewModel(app) {
    //    val btDevices: MutableLiveData<MutableList<BtDevice>> = MutableLiveData()
    val btDevices: MutableStateFlow<MutableList<BtDevice>> by lazy {
        MutableStateFlow<MutableList<BtDevice>>(mutableListOf()).also {
            viewModelScope.launch {
                BtUtils.scan(app, it)
                Log.d(TAG, "BtUtils scan start")
            }
        }
    }
//    val rawData =
//        Pager(PagingConfig(pageSize = PAGE_SIZE)) {
//            BtSource(app)
//        }.flow.cachedIn(viewModelScope)

    companion object {
        val TAG = "BtModel"
    }
}