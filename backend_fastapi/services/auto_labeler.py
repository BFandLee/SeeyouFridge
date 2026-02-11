import os
import numpy as np
from datetime import datetime

class AutoLabelerSeg:
    def __init__(self, save_dir="auto_dataset_seg"):
        self.img_dir = os.path.join(save_dir, "images")
        self.lbl_dir = os.path.join(save_dir, "labels")
        os.makedirs(self.img_dir, exist_ok=True)
        os.makedirs(self.lbl_dir, exist_ok=True)
        
        # ★ 중요: YOLO 학습시킬 때 data.yaml의 순서와 똑같이 맞춰야 함!
        self.class_map = {"egg": 0, "meat": 1, "onion": 2, "green_onion": 3, "garlic": 4, "tomato": 5, "potato": 6,
                          "carrot": 7, "bell_pepper": 8, "mushroom": 9, "fish": 10, "tofu": 11, "cabbage": 12, "cucumber": 13, "chili_pepper": 14,
                          "bread": 15, "cheese": 16, "apple": 17, "milk": 18} 

    def save_data(self, original_image_bytes, polygon_coords, class_name):
        if class_name not in self.class_map:
            return # 모르는 클래스는 저장 안 함

        class_id = self.class_map[class_name]
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
        filename = f"auto_{timestamp}"

        # 1. 이미지 저장
        with open(os.path.join(self.img_dir, f"{filename}.jpg"), "wb") as f:
            f.write(original_image_bytes)

        # 2. 라벨 저장 (YOLO Segmentation Format)
        # 포맷: <class-id> <x1> <y1> <x2> <y2> ...
        flat_coords = np.array(polygon_coords).flatten()
        coords_str = " ".join([f"{num:.6f}" for num in flat_coords])
        
        with open(os.path.join(self.lbl_dir, f"{filename}.txt"), "w") as f:
            f.write(f"{class_id} {coords_str}\n")
            
        print(f"✅ [Auto-Labeling] {class_name} 데이터 저장 완료 ({filename})")