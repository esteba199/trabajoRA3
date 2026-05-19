#!/bin/bash
# ==============================================================================
# Helper script to rebuild and restart all services (clean, no-cache build)
# ==============================================================================

echo "[+] Rebuilding and restarting all containers with no-cache..."

# Ensure .env exists
if [ ! -f .env ]; then
  if [ -f .env.example ]; then
    cp .env.example .env
  fi
fi

# Stop existing services
docker-compose down

# Build containers clean
docker-compose build --no-cache

# Run environment
docker-compose up -d

echo "[+] Rebuild completed and services launched."
echo "[+] Use 'docker-compose logs -f' to monitor boot progress."
