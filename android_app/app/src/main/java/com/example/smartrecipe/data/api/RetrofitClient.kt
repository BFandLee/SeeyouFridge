package com.example.smartrecipe.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Hugging face를 통한 배포 주소 https://alphgo-smart-recipe-server.hf.space <-- 허깅 페이스 주소
    private const val BASE_URL = "  https://ba56ebc92078.ngrok-free.app/"

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