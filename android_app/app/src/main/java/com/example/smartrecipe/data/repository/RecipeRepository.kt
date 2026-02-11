package com.example.smartrecipe.data.repository

import com.example.smartrecipe.data.api.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class RecipeRepository {
    private val api = RetrofitClient.apiService

    // 1. 재료 이미지 업로드
    suspend fun uploadIngredientImage(file: File) = try {
        val part = prepareFilePart("file", file)
        val response = api.getIngredients(part)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }




    // [내부 함수] 파일을 MultipartBody.Part로 변환하는 기계
    private fun prepareFilePart(partName: String, file: File): MultipartBody.Part {
        // 이미지 파일임을 명시 (image/jpeg 등)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
}