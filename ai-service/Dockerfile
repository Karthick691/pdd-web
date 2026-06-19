FROM python:3.12-slim

WORKDIR /app

# Install system dependencies for PyTorch and YOLOv8
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    libgomp1 \
    libglib2.0-0 \
    libgl1 \
    libxcb1 \
    libx11-xcb1 \
    libxext6 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .

# Install PyTorch CPU-only first (saves ~2GB vs CUDA version)
RUN pip install --no-cache-dir \
    torch torchvision --index-url https://download.pytorch.org/whl/cpu

# Install remaining requirements
RUN pip install --no-cache-dir -r requirements.txt

# Create a non-root user for security (M-4)
RUN adduser --disabled-password --gecos '' appuser

COPY . .

# Pre-download YOLOv8 model during build
RUN python -c "from ultralytics import YOLO; YOLO('yolov8n.pt')"

# Change ownership of app directory to appuser
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

EXPOSE 8002

# Cloud Run sets $PORT automatically; default to 8002 for local dev
CMD ["sh", "-c", "uvicorn main:app --host 0.0.0.0 --port ${PORT:-8002}"]
