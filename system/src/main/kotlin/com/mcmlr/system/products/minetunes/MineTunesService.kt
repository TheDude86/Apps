package com.mcmlr.system.products.minetunes

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadService {

    @Streaming
    @GET
    suspend fun download(@Url fileUrl: String): Response<ResponseBody>
}