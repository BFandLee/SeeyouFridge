from PIL import Image
import io

class IngredientService:
    def __init__(self, yolo, llm, auto_labeler):
        self.yolo = yolo
        self.llm = llm
        self.auto_labeler = auto_labeler
    

    async def predict_and_process(self, image_bytes: bytes):
        # 1. 이미지 전처리 (회전 보정)
        image = self.yolo.preprocess_image(image_bytes)
        
        # 2. 디버깅용 이미지 저장 (main.py에서 다운로드 가능하게)
        image.save("debug_view.jpg") 

        # 3. YOLO 추론
        results = self.yolo.detect(image)
        
        final_ingredients = []

        for result in results:
            if result.masks is None: continue # 세그멘테이션 실패시 패스

            # 박스, 마스크 좌표, 클래스 정보를 한 번에 순회
            for box, mask_poly in zip(result.boxes, result.masks.xyn):
                cls_id = int(box.cls[0])
                name = result.names[cls_id]
                conf = float(box.conf[0])
                
                # --- [전략] 하이브리드 보정 ---
                # 신뢰도가 낮거나(0.5 미만), 애매한 클래스(봉투, 통)인 경우
                if conf < 0.5:
                    
                    # (1) 해당 부분만 잘라내기 (Crop)
                    # box.xyxy는 [x1, y1, x2, y2] 좌표 (픽셀 단위)
                    x1, y1, x2, y2 = map(int, box.xyxy[0])
                    cropped_img = image.crop((x1, y1, x2, y2))
                    
                    # (2) LLM에게 물어보기
                    refined_name = await self.llm.identify_ingredient(
                        cropped_img, 
                        prompt="Identify this food ingredient inside the container/bag. Answer in one word (e.g., egg, onion)."
                    )
                    
                    # (3) 정답을 얻었다면? -> 자동 라벨링 저장! (선순환)
                    if refined_name != "unknown":
                        self.auto_labeler.save_data(image_bytes, mask_poly, refined_name)
                        final_ingredients.append(refined_name)
                    else:
                        final_ingredients.append(name) # LLM도 모르면 그냥 YOLO 결과 사용
                
                else:
                    # YOLO가 확신하면 그대로 사용
                    final_ingredients.append(name)

        return list(set(final_ingredients)) # 중복 제거 후 반환