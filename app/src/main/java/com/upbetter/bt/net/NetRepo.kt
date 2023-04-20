package com.upbetter.bt.net

import com.upbetter.bt.data.DataBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetRepo {
    suspend fun getData(
        pageCount: Int = 30,
        page: Int = 1
    ): DataBean {
        return withContext(Dispatchers.IO) {
            ServiceCreator.create<NetApi>().getImgs(pageCount, (page - 1) * pageCount)
        }
    }
}

object ServiceCreator {
    private const val BASE_URL = "https://service.picasso.adesk.com"
    private const val BASE_URL_1 = "http://124.223.197.120:83/chfs/shared/new/%E7%94%B0%E9%9C%87.mp3"

    val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
        GsonConverterFactory.create()
    ).build()

    inline fun <reified T> create(): T = retrofit.create(T::class.java)
}