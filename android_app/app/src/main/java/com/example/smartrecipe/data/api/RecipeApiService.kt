package com.example.smartrecipe.data.api

import com.example.smartrecipe.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RecipeApiService {

    // 1. 재료 사진 전송 (이미지 -> 재료 리스트)
    @Multipart
    @POST("predict/ingredients")
    suspend fun getIngredients(
        @Part image: MultipartBody.Part
    ): IngredientResponse

    // 2. [신규] 레시피 목록 요청
    @POST("recommend/list")
    suspend fun getRecipeList(@Body request: RecipeListRequest): Response<RecipeListResponse>

    // 3. [신규] 레시피 상세 요청
    @POST("recommend/detail")
    suspend fun getRecipeDetail(@Body request: RecipeDetailRequest): Response<RecipeDetailResponse>
}