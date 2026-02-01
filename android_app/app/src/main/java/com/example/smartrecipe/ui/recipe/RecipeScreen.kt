package com.example.smartrecipe.ui.recipe

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    ingredients: List<String>,
    onBack: () -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    val state by viewModel.recipeState.collectAsState()

    // 뒤로가기 버튼 시 버튼 누르기 전 화면으로 돌아가기
    BackHandler {
        onBack()
    }

    // 화면 진입 시 레시피 요청 (한 번만 실행)
    LaunchedEffect(Unit) {
        viewModel.getRecipe(ingredients)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 추천 레시피") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val currentState = state) {
                is RecipeUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI 쉐프가 레시피를 생각 중입니다...")
                    }
                }
                is RecipeUiState.Error -> {
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is RecipeUiState.Success -> {
                    // 레시피 내용 출력
                    val recipe = currentState.recipe
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = recipe.dishName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("준비물", style = MaterialTheme.typography.titleMedium)
                        recipe.ingredients.forEach {
                            Text("• $it", style = MaterialTheme.typography.bodyLarge)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("조리 순서", style = MaterialTheme.typography.titleMedium)
                        recipe.recipeSteps.forEach {
                            Text(it, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}