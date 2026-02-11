package com.example.smartrecipe.ui.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RecipeDetailScreen(
    dishName: String,
    ingredients: List<String>,
    viewModel: RecipeViewModel = viewModel()
) {
    val uiState by viewModel.recipeState.collectAsState()

    // 화면 켜지자마자 상세 정보 요청
    LaunchedEffect(Unit) {
        viewModel.fetchRecipeDetail(dishName, ingredients)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is RecipeUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is RecipeUiState.DetailSuccess -> {
                val detail = (uiState as RecipeUiState.DetailSuccess).detail

                // 스크롤 가능한 컬럼 (ScrollView 역할)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()) // 스크롤 가능하게 설정
                ) {
                    Text(text = detail.dishName, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    SectionTitle("🛒 필요 재료")
                    detail.ingredientsNeeded.forEach { ingredient ->
                        Text("• $ingredient", style = MaterialTheme.typography.bodyLarge)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SectionTitle("🍳 조리 방법")
                    detail.recipeSteps.forEachIndexed { index, step ->
                        Text("${index + 1}. $step", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("💡 셰프의 팁", style = MaterialTheme.typography.titleMedium)
                            Text(detail.tips)
                        }
                    }
                }
            }
            is RecipeUiState.Error -> {
                Text("에러 발생", modifier = Modifier.align(Alignment.Center))
            }
            else -> {}
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(8.dp))
}