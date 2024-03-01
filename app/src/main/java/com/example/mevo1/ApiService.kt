package com.example.mevo1

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.Response

interface ApiService {
    @GET("wellington")
    suspend fun getWellingtonVehicles(): Response<ResponseBody>
}
