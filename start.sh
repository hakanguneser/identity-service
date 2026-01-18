#!/usr/bin/env bash
set -euo pipefail

#######################################
# CONFIG
#######################################
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="/etc/infra-env/identity.env"

# Git varsa commit hash ile tag'le, yoksa timestamp kullan
if git -C "$BASE_DIR" rev-parse --git-dir > /dev/null 2>&1; then
  IMAGE_TAG="$(git -C "$BASE_DIR" rev-parse --short HEAD)"
else
  IMAGE_TAG="$(date +%Y%m%d%H%M%S)"
fi

IMAGE_NAME="identity:${IMAGE_TAG}"
IMAGE_LATEST="identity:latest"

#######################################
# LOG
#######################################
echo "======================================"
echo "🚀 Identity Deploy Starting"
echo "📁 Base dir  : $BASE_DIR"
echo "📄 Env file  : $ENV_FILE"
echo "🐳 Image     : $IMAGE_NAME"
echo "======================================"

#######################################
# ROOT CHECK
#######################################
if [ "$EUID" -ne 0 ]; then
  echo "🔐 Re-running with sudo..."
  exec sudo "$0" "$@"
fi

cd "$BASE_DIR"

#######################################
# ENV FILE CHECK
#######################################
if [ ! -f "$ENV_FILE" ]; then
  echo "❌ Env file not found: $ENV_FILE"
  exit 1
fi

chmod 640 "$ENV_FILE"

#######################################
# STOP OLD CONTAINERS
#######################################
echo "🛑 Stopping existing containers..."
docker compose --env-file "$ENV_FILE" down || true

#######################################
# BUILD IMAGE (NO CACHE)
#######################################
echo "🔨 Building Docker image (no cache)..."
docker build \
  --no-cache \
  -t "$IMAGE_NAME" \
  -t "$IMAGE_LATEST" \
  .

#######################################
# START STACK
#######################################
echo "▶ Starting identity stack..."
docker compose \
  --env-file "$ENV_FILE" \
  up -d

#######################################
# HEALTH CHECK
#######################################
sleep 5

if ! docker ps | grep -q identity; then
  echo "❌ Identity container not running"
  docker compose --env-file "$ENV_FILE" logs --tail=100
  exit 1
fi

#######################################
# INFO
#######################################
RUNNING_IMAGE=$(docker inspect identity --format='{{.Config.Image}}' 2>/dev/null || true)

echo "======================================"
echo "✅ Identity started successfully"
echo "🐳 Running image: $RUNNING_IMAGE"
echo "======================================"
