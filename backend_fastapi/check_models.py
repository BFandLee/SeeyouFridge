import google.generativeai as genai
import os
from dotenv import load_dotenv

load_dotenv()
api_key = os.getenv("GEMINI_API_KEY")

if not api_key:
    print("❌ API 키가 없습니다. .env 파일을 확인하세요.")
else:
    genai.configure(api_key=api_key)
    print("🔍 내 키로 사용 가능한 모델 목록을 조회합니다...\n")
    
    try:
        count = 0
        for m in genai.list_models():
            # 'generateContent' 기능이 있는 모델만 필터링 (채팅/텍스트 생성용)
            if 'generateContent' in m.supported_generation_methods:
                print(f"✅ 모델 이름: {m.name}")
                count += 1
        
        if count == 0:
            print("😱 사용 가능한 모델이 하나도 없습니다. (API 키 권한 문제일 수 있음)")
            
    except Exception as e:
        print(f"❌ 목록 조회 실패: {e}")