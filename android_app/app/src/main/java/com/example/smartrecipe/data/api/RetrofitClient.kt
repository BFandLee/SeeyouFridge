package com.example.smartrecipe.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // ★ 주의: 에뮬레이터라면 "http://10.0.2.2:8000/"
    // ★ 주의: 실제 폰이라면 "http://192.168.X.X:8000/" (CMD에서 ipconfig 확인)
    private const val BASE_URL = " https://dad9a1e47070.ngrok-free.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 로그창에 통신 내용 다 보여줌
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃 30초
        .readTimeout(30, TimeUnit.SECONDS)    // 읽기 타임아웃 30초 (AI가 느릴 수 있으니 넉넉하게)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: RecipeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecipeApiService::class.java)
    }
}