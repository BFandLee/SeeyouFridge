package com.example.smartrecipe.data.model

object IngredientTranslator {
    // 여기에 학습시킨 클래스 이름(영어)과 보여줄 이름(한글)을 적습니다.
    private val dictionary = mapOf(
        "egg" to "계란",
        "meat" to "고기",
        "onion" to "양파",
        "green_onion" to "파",
        "garlic" to "마늘",
        "tomato" to "토마토",
        "potato" to "감자",
        "carrot" to "당근",
        "bell_pepper" to "파프리카",
        "mushroom" to "버섯",
        "fish" to "생선",
        "tofu" to "두부",
        "cabbage" to "배추",
        "cucumber" to "오이",
        "bread" to "빵",
        "apple" to "사과",
        "milk" to "우유"
    )

    fun toKorean(englishName: String): String {
        // 1. 소문자로 변환 (대소문자 실수 방지)
        // 2. 혹시 모르니 공백/언더스코어 처리 (green_onion -> green onion 등 필요시 로직 추가)
        val key = englishName.lowercase().trim()

        // 3. 사전에서 찾아서 반환 (없으면 원래 영어 단어 반환)
        return dictionary[key] ?: englishName
    }
}