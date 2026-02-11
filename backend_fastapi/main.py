from fastapi import FastAPI, File, UploadFile
from fastapi.responses import FileResponse
from contextlib import asynccontextmanager
from dotenv import load_dotenv

# 서비스들 임포트
from services.yolo_service import YoloService
from services.llm_service import LlmService
from services.ocr_service import OcrService
from services.auto_labeler import AutoLabelerSeg
from services.ingredient_service import IngredientService
from schemas import RecipeListRequest, RecipeDetailRequest # 요청용 스키마

load_dotenv()
services = {}

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("🚀 서버 부팅 중... 모델 로딩 시작")
    yolo = YoloService()
    llm = LlmService()
    ocr = OcrService()
    labeler = AutoLabelerSeg()
    
    # 총괄 서비스에 의존성 주입
    services["ingredient"] = IngredientService(yolo, llm, labeler)
    services["llm"] = llm # 레시피용으로 따로 또 씀
    
    yield
    print("💤 서버 종료")
    services.clear()

app = FastAPI(lifespan=lifespan)

# --- [API 1] 재료 인식 (하이브리드 + 자동학습) ---
@app.post("/predict/ingredients")
async def predict_ingredients(file: UploadFile = File(...)):
    image_data = await file.read()
    ingredients = await services["ingredient"].predict_and_process(image_data)
    return {"status": "success", "ingredients": ingredients}

# --- [API 2] 디버깅 이미지 확인 ---
@app.get("/debug/image")
async def get_debug_image():
    try:
        return FileResponse("debug_view.jpg")
    except:
        return {"error": "No image found"}

# --- [API 3] 레시피 추천 ---
# [화면 1] 재료 목록 -> 요리 3가지 추천 (간단 리스트)
@app.post("/recommend/list")
async def recommend_list(request: RecipeListRequest):
    # LangChain Service 호출
    result = await services["llm"].get_recipe_list(request.ingredients)
    
    # Pydantic V2: 객체를 dict로 변환해서 리턴 (.model_dump 사용)
    return result.model_dump()

# [화면 2] 요리 선택 -> 상세 레시피 (조리법, 팁 등)
@app.post("/recommend/detail")
async def recommend_detail(request: RecipeDetailRequest):
    # LangChain Service 호출
    result = await services["llm"].get_recipe_detail(request.dish_name, request.ingredients)
    
    # Pydantic V2 변환
    return result.model_dump()