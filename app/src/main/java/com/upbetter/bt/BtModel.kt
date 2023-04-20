package com.upbetter.bt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.upbetter.bt.bt.BtSource
import com.upbetter.bt.config.PAGE_SIZE

class BtModel(private val app: Application) : AndroidViewModel(app) {
    //    var btDevices:MutableLiveData<ArrayList<BtDevice>> = MutableLiveData()
    val rawData =
        Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            BtSource(app)
        }.flow.cachedIn(viewModelScope)

}