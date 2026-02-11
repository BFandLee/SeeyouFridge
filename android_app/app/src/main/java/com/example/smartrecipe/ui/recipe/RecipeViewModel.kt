package com.example.smartrecipe.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartrecipe.data.api.RetrofitClient
import com.example.smartrecipe.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeViewModel : ViewModel() {
    private val api = RetrofitClient.apiService

    // [1] 내부 수정용 (MutableStateFlow) - 나만 쓸 수 있음
    private val _recipeState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)

    // [2] 외부 공개용 (StateFlow) - 액티비티가 얘를 쳐다봄
    val recipeState: StateFlow<RecipeUiState> = _recipeState


    // [기능 1] 목록 가져오기
    fun fetchRecipeList(ingredients: List<String>) {
        viewModelScope.launch {
            // [1] 로딩 시작 상태로 변경 (화면에서 뱅글이 돌아감)
            _recipeState.value = RecipeUiState.Loading
            try {
                val response = api.getRecipeList(RecipeListRequest(ingredients))
                if (response.isSuccessful) {
                    val list = response.body()?.recommendations
                    if (list != null) {
                        // [2] 성공 상태로 변경 (데이터 담아서 보냄)
                        _recipeState.value = RecipeUiState.ListSuccess(list)
                    } else {
                        // 응답은 왔는데 내용이 비었을 경우
                        _recipeState.value = RecipeUiState.Error("추천 레시피가 없습니다.")
                    }
                } else {
                    // [3] 서버 에러 (400, 500 등)
                    _recipeState.value = RecipeUiState.Error("서버 오류: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // [4] 네트워크 에러 (인터넷 끊김 등) -> 중요! 이걸 해야 흰 화면이 안 뜸
                _recipeState.value = RecipeUiState.Error("네트워크 오류: ${e.message}")
            }
        }
    }

    // [기능 2] 상세 정보 가져오기
    fun fetchRecipeDetail(dishName: String, ingredients: List<String>) {
        viewModelScope.launch {

            // [1] 로딩 시작 상태로 변경 (화면에서 뱅글이 돌아감)
            _recipeState.value = RecipeUiState.Loading
            try {
                // 2. 서버 요청
                val response = api.getRecipeDetail(RecipeDetailRequest(dishName, ingredients))

                if (response.isSuccessful) {
                    val detail = response.body()
                    if (detail != null) {
                        // 3. 성공 시: DetailSuccess 상태로 변경 (상세 데이터 포장해서 보냄)
                        _recipeState.value = RecipeUiState.DetailSuccess(detail)
                    } else {
                        // 데이터가 비었을 때
                        _recipeState.value = RecipeUiState.Error("상세 정보를 불러오지 못했습니다.")
                    }
                } else {
                    // 4. 서버 에러 (404, 500 등)
                    _recipeState.value = RecipeUiState.Error("서버 오류: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 5. 네트워크 에러 (인터넷 끊김 등) -> 이걸 해야 에러 화면이 뜸
                _recipeState.value = RecipeUiState.Error("네트워크 오류: ${e.message}")
            }
        }
    }
}

sealed interface RecipeUiState {
    object Loading : RecipeUiState
    object Idle : RecipeUiState
    data class ListSuccess(val recipes: List<RecipeSummary>) : RecipeUiState
    data class DetailSuccess(val detail: RecipeDetailResponse) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}