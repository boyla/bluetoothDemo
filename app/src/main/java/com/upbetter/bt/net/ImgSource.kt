package com.upbetter.bt.net

import android.util.Log
import android.widget.Toast
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.upbetter.bt.data.DataBean
import com.upbetter.bt.util.ToastUtil
import retrofit2.HttpException
import java.io.IOException

/**
 * Sample page-keyed PagingSource, which uses Int page number to load pages.
 *
 * Loads Items from network requests via Retrofit to a backend service.
 *
 * Note that the key type is Int, since we're using page number to load a page.
 */
class ImgSource(val pageSize: Int) : PagingSource<Int, DataBean.ResBean.Item>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DataBean.ResBean.Item> {

        return try {
            val pageNumber = params.key ?: 1
            ToastUtil.showToast(ToastUtil.ctx!!, "fetch page: $pageNumber")
            Log.d("ImgSource", "fetch page: $pageNumber")
            val response = NetRepo.getData(pageSize, pageNumber)
            if (response.code != 0) {
                LoadResult.Error(Exception(response.msg))
            } else {
                val prevKey = if (pageNumber > 1) pageNumber - 1 else null
                val nextKey =
                    if (response.res?.vertical?.isNotEmpty() == true) pageNumber + 1 else null
                LoadResult.Page(
                    data = response.res?.vertical ?: ArrayList(),
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, DataBean.ResBean.Item>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }
}