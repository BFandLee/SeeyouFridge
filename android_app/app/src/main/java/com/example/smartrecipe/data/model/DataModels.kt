package com.example.smartrecipe.data.model

import com.google.gson.annotations.SerializedName

// 1. 재료 인식 결과 (YOLO)
data class IngredientResponse(
    val status: String,
    val ingredients: List<String> // ["onion", "egg"]
)

// 2. 라벨 인식 결과 (OCR + LLM)
data class LabelResponse(
    val status: String,
    val result: LabelResult // 안에 또 다른 객체가 들어있음
)

data class LabelResult(
    @SerializedName("product_name") val productName: String,
    @SerializedName("brand") val brand: String?,
    @SerializedName("expiration_date") val expirationDate: String?
)