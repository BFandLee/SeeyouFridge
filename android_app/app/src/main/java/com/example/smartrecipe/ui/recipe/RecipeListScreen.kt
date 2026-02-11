package com.example.smartrecipe.ui.recipe

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartrecipe.data.model.RecipeSummary


@Composable
fun RecipeListScreen(
    ingredients: List<String>, // 메인에서 넘겨받은 재료 리스트
    viewModel: RecipeViewModel = viewModel(),
    onNavigateToDetail: (String, List<String>) -> Unit // 상세 화면으로 가는 네비게이터
) {
    // 1. 상태 관찰 (ViewModel의 StateFlow를 Compose 상태로 변환)
    val uiState by viewModel.recipeState.collectAsState()

    // [디버깅용 로그] 화면이 그려질 때마다 상태를 출력
    Log.d("RecipeList", "현재 상태: $uiState")
    Log.d("RecipeList", "받은 재료: $ingredients")

    // 2. 화면이 처음 뜰 때 서버에 데이터 요청 (딱 한 번만 실행)
    LaunchedEffect(Unit) {
        viewModel.fetchRecipeList(ingredients)
    }

    // 3. UI 그리기
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is RecipeUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is RecipeUiState.ListSuccess -> {
                val recipes = (uiState as RecipeUiState.ListSuccess).recipes

                // ★ 여기가 RecyclerView + Adapter 역할!
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(recipes) { recipe ->
                        RecipeItemCard(recipe = recipe, onClick = {
                            onNavigateToDetail(recipe.dishName, ingredients)
                        })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            is RecipeUiState.Error -> {
                Text(
                    text = (uiState as RecipeUiState.Error).message,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }
}

// ★ 여기가 item_recipe.xml 역할!
@Composable
fun RecipeItemCard(recipe: RecipeSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = recipe.dishName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(text = recipe.cookingTime, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = recipe.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}