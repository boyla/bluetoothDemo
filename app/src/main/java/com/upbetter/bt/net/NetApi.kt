package com.upbetter.bt.net

import com.upbetter.bt.data.DataBean
import retrofit2.http.GET
import retrofit2.http.Query

val url =
    "http://service.picasso.adesk.com/v1/vertical/vertical?limit=30&skip=180&adult=false&first=0&order=hot"

interface NetApi {
    @GET("/v1/vertical/vertical")
    suspend fun getImgs(
        @Query("limit") limit: Int = 10,
        @Query("skip") skip: Int = 0,
        @Query("adult") adult: Boolean = true,
        @Query("first") first: Int = 0,
        @Query("order") order: String = "hot"
    ): DataBean
}