package com.example.smartrecipe

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.example.smartrecipe.ui.camera.CameraScreen
import com.example.smartrecipe.ui.recipe.RecipeScreen
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
    // 현재 화면 상태 (Camera 또는 Recipe)
    var currentScreen by remember { mutableStateOf("Camera") }
    // 레시피 화면으로 넘겨줄 재료 리스트
    var ingredientList by remember { mutableStateOf(listOf<String>()) }

    when (currentScreen) {
        "Camera" -> {
            CameraScreen(
                onNavigateToRecipe = { ingredients ->
                    ingredientList = ingredients
                    currentScreen = "Recipe"
                }
            )
        }
        "Recipe" -> {
            RecipeScreen(
                ingredients = ingredientList,
                onBack = { currentScreen = "Camera" }
            )
        }
    }
}