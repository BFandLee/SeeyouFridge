package com.example.smartrecipe.data.api

import com.example.smartrecipe.data.model.*
import okhttp3.MultipartBody
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

    // 2. 라벨 사진 전송 (이미지 -> 제품 정보)
    @Multipart
    @POST("predict/label")
    suspend fun getLabelInfo(
        @Part image: MultipartBody.Part
    ): LabelResponse

    // 3. 레시피 추천 요청 (재료 리스트 -> 레시피)
    @POST("recommend/recipe")
    suspend fun getRecipe(
        @Body request: RecipeRequest
    ): RecipeResponse
}