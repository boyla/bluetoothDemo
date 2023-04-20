package com.upbetter.bt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.upbetter.bt.config.PAGE_SIZE
import com.upbetter.bt.net.ImgSource

class MainViewModel : ViewModel() {
    val rawData =
        Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            ImgSource(PAGE_SIZE)
        }.flow.cachedIn(viewModelScope)

}