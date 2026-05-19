#!/bin/bash
# ==============================================================================
# Helper script to stop and clean up the Docker Compose environment
# ==============================================================================

echo "[+] Stopping and removing container orchestration services..."

docker-compose down

echo "[+] Services stopped successfully."
