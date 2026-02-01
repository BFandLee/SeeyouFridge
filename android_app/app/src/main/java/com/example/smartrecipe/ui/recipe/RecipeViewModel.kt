package com.example.smartrecipe.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartrecipe.data.model.RecipeResponse
import com.example.smartrecipe.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeViewModel : ViewModel() {
    private val repository = RecipeRepository()

    private val _recipeState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val recipeState: StateFlow<RecipeUiState> = _recipeState

    fun getRecipe(ingredients: List<String>) {
        viewModelScope.launch {
            _recipeState.value = RecipeUiState.Loading

            val result = repository.getRecipeRecommendation(ingredients)

            result.onSuccess { response ->
                _recipeState.value = RecipeUiState.Success(response)
            }.onFailure { e ->
                _recipeState.value = RecipeUiState.Error("레시피 생성 실패: ${e.message}")
            }
        }
    }
}

sealed interface RecipeUiState {
    object Loading : RecipeUiState
    data class Success(val recipe: RecipeResponse) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}