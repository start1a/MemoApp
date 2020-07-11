package com.start3a.memoji

import com.start3a.memoji.Model.RetrofitAPI
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitService {

    fun getService(): RetrofitAPI = retrofit.create(RetrofitAPI::class.java)

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://openapi.naver.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
}