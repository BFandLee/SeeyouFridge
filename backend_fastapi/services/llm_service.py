import google.generativeai as genai
import os
import json

class LlmService:
    def __init__(self):
        # 1. 환경변수에서 키 가져오기
        self.api_key = os.getenv("GEMINI_API_KEY")
        
        if not self.api_key:
            print("❌ 오류: .env 파일에 GEMINI_API_KEY가 없습니다.")
            return

        # 2. Gemini 설정
        try:
            genai.configure(api_key=self.api_key)
            
            # 3. 모델 초기화 (JSON 모드 강제 설정)
            # gemini-1.5-flash가 빠르고 가성비가 좋음
            self.model = genai.GenerativeModel(
                'models/gemini-2.5-flash',
                generation_config={"response_mime_type": "application/json"}
            )
            print("✅ Gemini 모델 로드 완료! (Model: gemini-1.5-flash)")
            
        except Exception as e:
            print(f"❌ Gemini 설정 중 오류 발생: {e}")

    def get_recipe(self, ingredients: list):
        """
        재료 리스트를 받아 레시피를 추천해주는 함수
        """
        if not ingredients:
            return {"error": "재료 목록이 비어있습니다."}

        # 프롬프트 (AI에게 시킬 명령)
        prompt = f"""
        너는 30년 경력의 한식 요리사야.
        지금 냉장고에 {', '.join(ingredients)} 재료가 있어.
        
        이 재료들을 메인으로 사용하는 맛있는 요리 레시피 1가지를 추천해줘.
        추가 양념(간장, 소금 등)은 집에 있다고 가정해.

        반드시 아래 JSON 형식으로만 답변해:
        {{
            "dish_name": "요리 이름",
            "ingredients": ["재료1", "재료2", "양념1"],
            "recipe_steps": ["1. 재료를 씻으세요.", "2. 물을 끓이세요."]
        }}
        """

        try:
            # AI에게 질문 던지기
            response = self.model.generate_content(prompt)
            
            # 결과 텍스트 받기
            response_text = response.text
            
            # JSON 문자열을 파이썬 딕셔너리로 변환
            recipe_data = json.loads(response_text)
            
            return recipe_data

        except Exception as e:
            print(f"❌ 레시피 생성 실패: {e}")
            # 에러가 나면 빈 값이라도 줘야 앱이 안 죽음
            return {
                "error": str(e),
                "dish_name": "알 수 없음",
                "ingredients": [],
                "recipe_steps": ["레시피를 생성하는 중 오류가 발생했습니다."]
            }
        
    def parse_ocr_result(self, ocr_text_list: list):
        """
        OCR로 읽은 지저분한 텍스트 리스트를 정리해서
        정확한 '제품명'과 '유통기한'을 뽑아내는 함수
        """
        if not ocr_text_list:
            return {"error": "텍스트 데이터가 없습니다."}

        prompt = f"""
        너는 마트 재고 관리 AI야.
        아래는 상품 포장지를 OCR(광학 문자 인식)로 읽은 결과 리스트야. 
        오타와 노이즈가 섞여 있어.
        
        문맥을 파악해서 정확한 '상품명(재료명)'과 '유통기한'을 추론해줘.
        유통기한이 없으면 null로 표시해.
        
        [OCR 결과]
        {', '.join(ocr_text_list)}
        
        반드시 아래 JSON 형식으로만 대답해:
        {{
            "product_name": "정확한 상품명 (예: 된장, 우유)",
            "brand": "브랜드명 (없으면 null)",
            "expiration_date": "YYYY-MM-DD (없으면 null)"
        }}
        """

        try:
            response = self.model.generate_content(prompt)
            return json.loads(response.text)
        except Exception as e:
            print(f"❌ OCR 파싱 실패: {e}")
            return {"product_name": "알 수 없음", "error": str(e)}