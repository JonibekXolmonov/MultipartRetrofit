package com.example.multipartretrofit.api

import com.example.multipartretrofit.model.ImageResponse
import retrofit2.Call
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

@JvmSuppressWildcards
interface ApiService {

    @POST("images/upload")
    fun uploadPhoto(@Body body: RequestBody): Call<ImageResponse>

}