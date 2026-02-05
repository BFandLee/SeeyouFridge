from ultralytics import YOLO
from PIL import Image, ImageOps
import io

class YoloService:
    def __init__(self, model_path="models/best.pt"):
        print(f"🔄 YOLO 모델 로딩 중... ({model_path})")
        try:
            self.model = YOLO(model_path)
            print("✅ YOLO 모델 로드 완료!")
        except Exception as e:
            print(f"❌ YOLO 모델 로드 실패: {e}")
            self.model = None

    def preprocess_image(self, image_bytes: bytes) -> Image.Image:
        """바이트 -> PIL 이미지 변환 + 회전 보정(갤럭시 이슈 해결)"""
        image = Image.open(io.BytesIO(image_bytes))
        image = ImageOps.exif_transpose(image) # ★ 핵심: 사진 일으켜 세우기
        return image

    def detect(self, image: Image.Image, conf=0.4):
        """추론 실행 및 결과 반환 (Segment 모델)"""
        if not self.model:
            return []
        return self.model(image, conf=conf)