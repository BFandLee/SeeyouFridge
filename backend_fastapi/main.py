from fastapi import FastAPI, File, UploadFile
from pydantic import BaseModel
from typing import List
from dotenv import load_dotenv
import contextlib

# 만든 서비스들 가져오기
from services.yolo_service import YoloService
from services.ocr_service import OcrService
from services.llm_service import LlmService

# 1. 환경변수 로드 (.env)
load_dotenv()

# 2. 전역 변수로 서비스 인스턴스 선언 (서버 켜질 때 채워짐)
models = {}

@contextlib.asynccontextmanager
async def lifespan(app: FastAPI):
    # --- [시작될 때 실행] ---
    print("🚀 서버 시작! 모델들을 메모리에 올립니다...")
    models["yolo"] = YoloService()
    models["ocr"] = OcrService() # OCR은 무거우니 필요할 때 켜거나 일단 주석 (테스트용)
    models["llm"] = LlmService()
    yield
    # --- [꺼질 때 실행] ---
    print("💤 서버 종료. 자원을 정리합니다.")
    models.clear()

app = FastAPI(lifespan=lifespan)

# --- [API 1] 재료 인식 ---
@app.post("/predict/ingredients")
async def predict_img(file: UploadFile = File(...)):
    image_data = await file.read()
    ingredients = models["yolo"].detect_ingredients(image_data)
    return {"status": "success", "ingredients": ingredients}

# --- [API 2] 레시피 추천 ---
class RecipeRequest(BaseModel):
    ingredients: List[str]

@app.post("/recommend/recipe")
async def recommend(request: RecipeRequest):
    recipe = models["llm"].get_recipe(request.ingredients)
    return recipe

# --- [API 3] 라벨 인식 (OCR) ---
# main.py 의 predict_label 함수 수정

@app.post("/predict/label")
async def predict_label(file: UploadFile = File(...)):
    # 1. 이미지 읽기
    image_data = await file.read()
    
    # 2. OCR로 글자 긁어오기 (눈)
    # result_list 예시: ["성정진", "된장", "청정원", ...]
    raw_text_list = models["ocr"].extract_text(image_data)
    
    # 3. LLM으로 정리하기 (뇌)
    # parsed_data 예시: {"product_name": "된장", "brand": "청정원", ...}
    parsed_data = models["llm"].parse_ocr_result(raw_text_list)
    
    return {
        "status": "success",
        "raw_text": raw_text_list, # 디버깅용 원본
        "result": parsed_data      # 깔끔하게 정리된 결과
    }