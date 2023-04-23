//package com.upbetter.bt.bt
//
//import android.content.Context
//import android.util.Log
//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import com.upbetter.bt.data.BtDevice
//import com.upbetter.bt.util.ToastUtil
//
//class BtSource(val appCtx: Context) : PagingSource<Int, BtDevice>() {
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BtDevice> {
//
//        return try {
//            val pageNumber = params.key ?: 1
//            ToastUtil.showToast(ToastUtil.ctx!!, "fetch page: $pageNumber")
//            Log.d("ImgSource", "fetch page: $pageNumber")
//            val deviceLi = BtRepo.getDevices(appCtx)
//            if (deviceLi.isEmpty()) {
//                LoadResult.Error(Exception("no device found"))
//            } else {
//                val prevKey = if (pageNumber > 1) pageNumber - 1 else null
//                val nextKey =
//                    if (pageNumber < deviceLi.size) pageNumber + 1 else null
//                LoadResult.Page(
//                    data = deviceLi,
//                    prevKey = prevKey,
//                    nextKey = nextKey
//                )
//            }
//        } catch (e: Exception) {
//            LoadResult.Error(e)
//        }
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, BtDevice>): Int? {
//        return state.anchorPosition?.let {
//            state.closestPageToPosition(it)?.prevKey?.plus(1)
//                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
//        }
//    }
//}