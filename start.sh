#!/usr/bin/env bash
set -e

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="/etc/infra-env/identity.env"
IMAGE_NAME="identity:latest"

echo "==> Identity startup"
echo "==> Base dir : $BASE_DIR"
echo "==> Env file : $ENV_FILE"
echo "==> Image    : $IMAGE_NAME"

# root değilsek sudo ile tekrar çalıştır
if [ "$EUID" -ne 0 ]; then
  exec sudo "$0" "$@"
fi

cd "$BASE_DIR"

# env file kontrol
if [ ! -f "$ENV_FILE" ]; then
  echo "❌ Env file not found: $ENV_FILE"
  exit 1
fi

chmod 640 "$ENV_FILE"

# image var mı? yoksa build et
if ! docker image inspect "$IMAGE_NAME" >/dev/null 2>&1; then
  echo "▶ Docker image not found, building..."
  docker build -t "$IMAGE_NAME" .
else
  echo "▶ Docker image already exists, skipping build"
fi

echo "▶ Starting identity stack"
docker compose --env-file "$ENV_FILE" up -d

sleep 3

docker ps | grep identity || {
  echo "❌ identity container not running"
  docker compose --env-file "$ENV_FILE" logs --tail=50
  exit 1
}

echo "✅ identity started successfully"
