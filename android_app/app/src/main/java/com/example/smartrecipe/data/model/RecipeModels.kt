package com.example.smartrecipe.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// --- [요청] ---
data class RecipeListRequest(
    val ingredients: List<String>
)

data class RecipeDetailRequest(
    @SerializedName("dish_name") val dishName: String,
    val ingredients: List<String>
)

// --- [응답 1: 목록] ---
data class RecipeListResponse(
    val recommendations: List<RecipeSummary>
)

data class RecipeSummary(
    val id: Int,
    @SerializedName("dish_name") val dishName: String,
    val description: String,
    @SerializedName("cooking_time") val cookingTime: String
) : Serializable // Intent로 넘기기 위해 직렬화

// --- [응답 2: 상세] ---
data class RecipeDetailResponse(
    @SerializedName("dish_name") val dishName: String,
    @SerializedName("ingredients_needed") val ingredientsNeeded: List<String>,
    @SerializedName("recipe_steps") val recipeSteps: List<String>,
    val tips: String
)