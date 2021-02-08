package com.lsrw.txasrdemo.net

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitCreator {
    const val baseUrl = "asr.tencentcloudapi.com/"
    const val tencentBaseUrl = "asr.tencentcloudapi.com"
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> create(serviceClass: Class<T>) = retrofit.create(serviceClass)

    inline fun <reified T> create():T = create(T::class.java)
}