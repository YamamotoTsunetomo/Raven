package com.tsunetomo.raven.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {
//    @GET("search?index=&")
    @GET("search")
    suspend fun getBook(
        @Query("q") query: String,
        @Query("ext") extensions: List<String>,
        @Query("sort") sortBy: String,
    ): Response<ResponseBody>


    @GET("{id}")
    suspend fun getBookInfo(
        @Path("id") id: String,
    ): Response<ResponseBody>

    @GET("{id}")
    suspend fun getMirrorPage(
        @Path("id") id: String,
    ): Response<ResponseBody>
}
