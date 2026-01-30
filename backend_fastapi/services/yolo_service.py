from ultralytics import YOLO
from PIL import Image
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

    def detect_ingredients(self, image_bytes: bytes):
        if not self.model:
            return []

        # 바이트 -> 이미지 변환
        image = Image.open(io.BytesIO(image_bytes))
        
        # 추론 (conf=0.4: 확신 40% 이상만)
        results = self.model(image, conf=0.4)
        
        detected = []
        for result in results:
            for box in result.boxes:
                cls_id = int(box.cls[0])
                name = result.names[cls_id]
                conf = float(box.conf[0])
                detected.append({"name": name, "confidence": round(conf, 2)})
        
        # 중복 제거 (set 활용)
        unique_names = list({item['name'] for item in detected})
        return unique_names