# 1. Python 3.10 버전의 가벼운 리눅스(Slim)를 베이스로 사용
FROM python:3.10-slim

# 2. 작업 폴더 설정 (이 안에서 모든 일이 일어남)
WORKDIR /app

# 3. [중요] OpenCV와 EasyOCR 실행에 필요한 시스템 라이브러리 설치
# (이게 없으면 'libGL.so not found' 에러가 뜨면서 앱이 죽어!)
RUN apt-get update && apt-get install -y \
    libgl1-mesa-glx \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# 4. 아까 만든 쇼핑 리스트(requirements.txt) 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. [Hugging Face 전용 설정]
# 보안상 root 권한으로 실행하면 안 됨. 새로운 사용자(user)를 만들어서 실행해야 함.
# 또한 EasyOCR이 모델을 다운로드할 때 권한 문제가 없도록 홈 디렉토리 권한 설정.
RUN useradd -m -u 1000 user
USER user
ENV HOME=/home/user \
    PATH=/home/user/.local/bin:$PATH

# EasyOCR 모델이 저장될 폴더 미리 생성 (권한 에러 방지)
RUN mkdir -p /home/user/.EasyOCR

# 6. 자네의 소스 코드 전체를 복사 (권한을 user에게 줌)
COPY --chown=user . .

# 7. Hugging Face는 무조건 7860 포트를 열어야 함 (규칙임!)
EXPOSE 7860

# 8. 서버 실행 명령
# (host는 0.0.0.0, port는 7860으로 설정해야 외부 접속 가능)
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "7860"]