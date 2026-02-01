package com.example.smartrecipe.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartrecipe.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class CameraViewModel : ViewModel() {
    private val repository = RecipeRepository()

    // 1. UI 상태 관리 (기본: 대기 중)
    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState

    // 2. 찾은 재료 목록 (장바구니)
    private val _ingredients = MutableStateFlow<List<String>>(emptyList())
    val ingredients: StateFlow<List<String>> = _ingredients

    // 1. [기능추가] 전체 초기화 (쓰레기통)
    fun clearIngredients() {
        _ingredients.value = emptyList()
    }
    // 2. [기능추가] 재료 하나 삭제 (X 버튼)
    fun removeIngredient(ingredient: String) {
        val current = _ingredients.value.toMutableList()
        current.remove(ingredient)
        _ingredients.value = current
    }

    // 3. [기능추가] 재료 수동 추가 (키보드로 입력)
    fun addIngredientManual(name: String) {
        if (name.isBlank()) return
        val current = _ingredients.value.toMutableList()
        if (!current.contains(name)) {
            current.add(name)
            _ingredients.value = current
        }
    }

    // 4. [기능수정] 결과가 오면 바로 담지 않고, UI에게 "검사해줘"라고 데이터만 넘김
    fun uploadIngredientImage(file: File) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Loading
            val result = repository.uploadIngredientImage(file)
            result.onSuccess { response ->
                // 바로 _ingredients에 넣지 않음! Success 상태에 담아서 보냄
                _uiState.value = CameraUiState.Success(response.ingredients)
            }.onFailure { e ->
                _uiState.value = CameraUiState.Error("인식 실패: ${e.message}")
            }
        }
    }

    // OCR + LLM
    fun uploadLabelImage(file: File) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Loading
            val result = repository.uploadLabelImage(file)
            result.onSuccess { response ->
                // 라벨은 하나만 나오지만 리스트 형태로 통일해서 보냄
                _uiState.value = CameraUiState.Success(listOf(response.result.productName))
            }.onFailure { e ->
                _uiState.value = CameraUiState.Error("라벨 인식 실패: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = CameraUiState.Idle
    }
}

sealed interface CameraUiState {
    object Idle : CameraUiState
    object Loading : CameraUiState
    // Success가 이제 'String 메시지'가 아니라 '감지된 재료 리스트'를 들고 있음
    data class Success(val detectedItems: List<String>) : CameraUiState
    data class Error(val message: String) : CameraUiState
}