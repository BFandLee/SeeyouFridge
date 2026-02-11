package com.example.smartrecipe.ui.camera

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import android.Manifest
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete


@Composable
fun CameraScreen(
    onNavigateToRecipe: (List<String>) -> Unit, // 레시피 화면으로 넘어가는 함수
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()

    // 검수용 팝업 상태 관리
    var showConfirmDialog by remember { mutableStateOf(false) }
    var detectedListForReview by remember { mutableStateOf(mutableListOf<String>()) }
    var manualInputText by remember { mutableStateOf("") }

    // 카메라 제어용 변수
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val previewView = remember { PreviewView(context) }

    // 로딩 중일 때 표시
    if (uiState is CameraUiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    // 에러나 성공 메시지 토스트 띄우기
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CameraUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            is CameraUiState.Success -> {
                // AI가 찾은 목록을 검수용 리스트에 복사
                detectedListForReview = state.detectedItems.toMutableList()
                showConfirmDialog = true // 팝업 오픈!
                viewModel.resetState() // 상태는 다시 대기로
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 카메라 미리보기 화면 (AndroidView 사용)
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
            update = {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    imageCapture = ImageCapture.Builder().build()

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraScreen", "카메라 바인딩 실패", e)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // 로딩 바
        if (uiState is CameraUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // 2. 상단: 초기화(휴지통) 버튼
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.clearIngredients() },
                containerColor = Color.Red,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Delete, contentDescription = "초기화")
            }
        }

        // 레시피 추천받기 버튼 (재료가 있을 때만 활성화)
        Button(
            onClick = { onNavigateToRecipe(ingredients) },
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            enabled = ingredients.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint=Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("이 재료로 요리 추천받기", color = Color.Black)
        }
        // 3. 하단 컨트롤 패널
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.7f))
                .navigationBarsPadding()        // 시스템 하단 바 높이만큼 자동으로 밀어줌
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // 현재 장바구니 리스트
            if (ingredients.isNotEmpty()) {
                Text("냉장고 속 재료 (터치해서 삭제):", color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ingredients) { item ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.removeIngredient(item) }, // 누르면 삭제
                            label = { Text(item) },
                            trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            } else {
                Text("재료를 인식하거나 추가해주세요.", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 버튼들 (재료촬영 / 라벨촬영 / 레시피추천)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { captureImage(context, imageCapture) { viewModel.uploadIngredientImage(it) } }) {
                    Text("재료 촬영")
                }

            }

            Spacer(modifier = Modifier.height(8.dp))


        }
    }

    // 검수용 팝업 (Human-in-the-loop)
        if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("인식 결과 확인") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("AI가 찾은 재료입니다. 맞는지 확인해주세요.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    // AI가 찾은 것들 (칩으로 표시)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        detectedListForReview.forEach { item ->
                            InputChip(
                                selected = true,
                                onClick = { detectedListForReview.remove(item) }, // 누르면 즉시 삭제
                                label = { Text(item) },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 수동 추가 입력창
                    Text("빠진 재료가 있나요?", style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = manualInputText,
                            onValueChange = { manualInputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("예: 고추") },
                            singleLine = true
                        )
                        IconButton(onClick = {
                            if (manualInputText.isNotBlank()) {
                                detectedListForReview.add(manualInputText)
                                manualInputText = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "추가")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    // 확인 누르면 실제 ViewModel 리스트에 반영
                    detectedListForReview.forEach { viewModel.addIngredientManual(it) }
                    showConfirmDialog = false
                }) {
                    Text("장바구니 담기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

// FlowRow가 없는 구버전 Compose라면 LazyRow나 Column으로 대체 가능하지만,
// 최신 버전엔 FlowRow 사용 권장. 없으면 아래 임시 구현 사용.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    horizontalArrangement: Arrangement.Horizontal,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}


// [내부 함수] 사진 찍어서 파일로 저장하는 로직
private fun captureImage(
    context: Context,
    imageCapture: ImageCapture?,
    onImageCaptured: (File) -> Unit
) {
    val imageCapture = imageCapture ?: return

    // 저장할 파일 생성
    val photoFile = File(
        context.externalCacheDir,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                // 사진 저장 성공 시 콜백 호출
                onImageCaptured(photoFile)
            }

            override fun onError(exc: ImageCaptureException) {
                Log.e("CameraScreen", "사진 촬영 실패: ${exc.message}", exc)
            }
        }
    )
}