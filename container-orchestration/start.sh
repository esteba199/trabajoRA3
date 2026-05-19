#!/bin/bash
# ==============================================================================
# Helper script to start the complete Docker Compose multi-tier application
# ==============================================================================

echo "[+] Starting Optimized Container Orchestration Environment..."

# Ensure .env exists, if not warn and exit
if [ ! -f .env ]; then
  if [ -f .env.example ]; then
    echo "[-] .env file not found. Creating from .env.example..."
    cp .env.example .env
  else
    echo "[!] ERROR: Neither .env nor .env.example found. Aborting."
    exit 1
  fi
fi

# Run docker-compose
docker-compose up -d

echo "[+] Application environment started successfully!"
echo "[+] Access URLs:"
echo "    - Web Application: http://localhost"
echo "    - Backend Status API: http://localhost/api/status"
echo "[+] Run './stop.sh' to stop the environment, or 'docker-compose logs -f' to inspect logs."
