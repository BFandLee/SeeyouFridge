import os
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import PydanticOutputParser
from schemas import RecipeListResponse, RecipeDetailResponse

class LlmService:
    def __init__(self):
        self.llm = ChatGoogleGenerativeAI(
            model="gemini-1.5-flash",
            temperature=0.1,
            api_key=os.getenv("GEMINI_API_KEY")
        )

    # [기능 1] 요리 목록 추천
    async def get_recipe_list(self, ingredients: list):
        # Pydantic V2 객체를 넣어주면 알아서 인식합니다.
        parser = PydanticOutputParser(pydantic_object=RecipeListResponse)

        prompt = ChatPromptTemplate.from_template("""
        너는 한식 전문 셰프야.
        냉장고 재료: {ingredients}
        이 재료들로 만들 수 있는 요리 3가지를 추천해줘.
        
        {format_instructions}
        """)

        chain = prompt | self.llm | parser

        return await chain.ainvoke({
            "ingredients": ", ".join(ingredients),
            "format_instructions": parser.get_format_instructions()
        })

    # [기능 2] 상세 레시피 생성
    async def get_recipe_detail(self, dish_name: str, ingredients: list):
        parser = PydanticOutputParser(pydantic_object=RecipeDetailResponse)

        prompt = ChatPromptTemplate.from_template("""
        사용자가 선택한 요리: '{dish_name}'
        현재 가용 재료: {ingredients}
        이 요리의 상세 레시피를 작성해줘. 기본 양념은 집에 있다고 가정해.
        
        {format_instructions}
        """)

        chain = prompt | self.llm | parser

        return await chain.ainvoke({
            "dish_name": dish_name,
            "ingredients": ", ".join(ingredients),
            "format_instructions": parser.get_format_instructions()
        })