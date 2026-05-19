#!/bin/bash
# ==============================================================================
# Automated MySQL Backup Script for 2º DAW Container Orchestration
# Saves timestamped backups to ./backups/ directory using docker exec
# ==============================================================================

# Exit immediately if a command exits with a non-zero status
set -e

# Configuration
BACKUP_DIR="./backups"
DB_CONTAINER_NAME="daw-mysql-db"
DB_NAME="daw_orchestration_db"
DB_USER="root"
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
BACKUP_FILE="${BACKUP_DIR}/backup_${TIMESTAMP}.sql"

# Load environment variables from .env if present
if [ -f .env ]; then
  # Read .env file line by line, ignore comments and empty lines
  export $(grep -v '^#' .env | xargs)
  # Override DB credentials from environment if they are set
  DB_NAME=${MYSQL_DATABASE:-$DB_NAME}
  DB_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
fi

# Ensure backups directory exists
mkdir -p "${BACKUP_DIR}"

echo "[+] Starting database backup process..."
echo "[+] Target Container: ${DB_CONTAINER_NAME}"
echo "[+] Target Database: ${DB_NAME}"
echo "[+] Backup Destination: ${BACKUP_FILE}"

# Check if MySQL container is running
if [ ! "$(docker ps -q -f name=${DB_CONTAINER_NAME})" ]; then
    echo "[!] ERROR: MySQL container '${DB_CONTAINER_NAME}' is not running."
    exit 1
fi

# Perform mysqldump inside the container and stream to host backup file
# Using -p"" with env variable for non-interactive execution
docker exec -i "${DB_CONTAINER_NAME}" mysqldump \
    -u "${DB_USER}" \
    -p"${DB_ROOT_PASSWORD}" \
    --databases "${DB_NAME}" \
    --single-transaction \
    --quick \
    --lock-tables=false > "${BACKUP_FILE}"

# Check if backup file is created and has size > 0
if [ -s "${BACKUP_FILE}" ]; then
    echo "[+] Backup completed successfully!"
    echo "[+] File size: $(du -sh ${BACKUP_FILE} | cut -f1)"
else
    echo "[!] ERROR: Backup file is empty. Something went wrong during mysqldump."
    rm -f "${BACKUP_FILE}"
    exit 1
fi

# Optional: Keep only the last 7 backups to save disk space
echo "[+] Cleaning up backups older than 7 days..."
find "${BACKUP_DIR}" -name "backup_*.sql" -type f -mtime +7 -delete

echo "[+] Database backup pipeline completed."
