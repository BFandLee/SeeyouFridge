from pydantic import BaseModel, Field 
from typing import List

# --- [API 1: 목록 조회용 구조] ---
class RecipeSummary(BaseModel):
    id: int = Field(description="1부터 시작하는 순번")
    dish_name: str = Field(description="요리 이름")
    description: str = Field(description="요리의 특징을 묘사한 한 줄 설명")
    cooking_time: str = Field(description="예상 조리 시간 (예: 15분)")

class RecipeListResponse(BaseModel):
    recommendations: List[RecipeSummary] = Field(description="추천 요리 3가지 리스트")

# --- [API 2: 상세 조회용 구조] ---
class RecipeDetailResponse(BaseModel):
    dish_name: str = Field(description="요리 이름")
    ingredients_needed: List[str] = Field(description="필요한 재료 목록 (양념 포함)")
    recipe_steps: List[str] = Field(description="단계별 조리법")
    tips: str = Field(description="맛있게 만드는 셰프의 팁")