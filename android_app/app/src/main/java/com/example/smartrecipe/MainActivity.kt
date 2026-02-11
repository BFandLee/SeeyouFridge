package com.example.smartrecipe

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartrecipe.ui.camera.CameraScreen
import com.example.smartrecipe.ui.recipe.RecipeDetailScreen
import com.example.smartrecipe.ui.recipe.RecipeListScreen
import com.example.smartrecipe.ui.theme.SmartRecipeTheme

class MainActivity : ComponentActivity() {

    // 권한 요청 처리기
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // 권한 거부 시 처리 (간단히 종료하거나 메시지)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 앱 켜지자마자 카메라 권한 요청
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        setContent {
            SmartRecipeTheme {
                SmartRecipeApp()
            }
        }
    }
}

@Composable
fun SmartRecipeApp() {
    val navController = rememberNavController()

    // 네비게이션 경로 정의
    // 1. startDestination: 앱 켜지면 제일 먼저 보여줄 화면 -> "camera"
    NavHost(navController = navController, startDestination = "camera") {

        // [화면 1] 카메라 화면
        composable("camera") {
            CameraScreen(
                onNavigateToRecipe = { ingredients ->
                    // 재료 리스트를 문자열(예: "egg,onion")로 바꿔서 다음 화면에 넘김
                    val ingredientsString = ingredients.joinToString(",")
                    navController.navigate("recipeList/$ingredientsString")
                }
            )
        }

        // [화면 2] 레시피 목록 화면
        // 주소 형식: "recipeList/{ingredients}"
        composable(
            route = "recipeList/{ingredients}",
            arguments = listOf(navArgument("ingredients") { type = NavType.StringType })
        ) { backStackEntry ->
            // URL에서 재료 꺼내기
            val ingredientsString = backStackEntry.arguments?.getString("ingredients") ?: ""
            val ingredients = ingredientsString.split(",").filter { it.isNotEmpty() }

            RecipeListScreen(
                ingredients = ingredients,
                onNavigateToDetail = { dishName, currentIngredients ->
                    val ingString = currentIngredients.joinToString(",")
                    navController.navigate("recipeDetail/$dishName/$ingString")
                }
            )
        }

        // [화면 3] 레시피 상세 화면
        // 주소 형식: "recipeDetail/{dishName}/{ingredients}"
        composable(
            route = "recipeDetail/{dishName}/{ingredients}",
            arguments = listOf(
                navArgument("dishName") { type = NavType.StringType },
                navArgument("ingredients") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dishName = backStackEntry.arguments?.getString("dishName") ?: ""
            val ingredientsString = backStackEntry.arguments?.getString("ingredients") ?: ""
            val ingredients = ingredientsString.split(",").filter { it.isNotEmpty() }

            RecipeDetailScreen(
                dishName = dishName,
                ingredients = ingredients
            )
        }
    }
}