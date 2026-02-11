import easyocr

class OcrService:
    def __init__(self):
        print("🔄 EasyOCR 모델 로딩 중... (시간이 좀 걸립니다)")
        # 메모리 절약을 위해 gpu=False로 할 수도 있음. GPU 있으면 True 추천.
        self.reader = easyocr.Reader(['ko', 'en'], gpu=True) 
        print("✅ EasyOCR 로드 완료!")

    def extract_text(self, image_bytes: bytes):
        # easyocr은 파일 경로, url, bytes, numpy array 다 받음
        result = self.reader.readtext(image_bytes, detail=0) # detail=0은 텍스트만 리턴
        return result