# Administration & Maintenance Manual
## Optimized Container Orchestration for Web Applications

This administration guide outlines day-to-day operations, system maintenance, database backups, disaster recovery, and structural scaling configurations.

---

## 1. System Monitoring and Operations

Managing the active stack involves key Docker commands:

### A. Check Container Resource Performance
To monitor memory consumption, CPU utilization, and I/O metrics in real time:
```bash
docker stats
```
This is critical for observing our enforced resource limits (defined in `docker-compose.yml`) under load.

### B. Display Running Stack Processes
```bash
docker-compose top
```

### C. Active Log Inspection
Monitor all services:
```bash
docker-compose logs -f
```
Filter logs for a specific service:
```bash
docker-compose logs -f backend
```

---

## 2. Advanced Security Implementations

This project implements strict enterprise security methodologies:

### A. Non-Root Execution Rules
Standard container deployments run internally as `root` (UID 0). In the case of an application vulnerability exploit (e.g., remote code execution), the attacker gains immediate root control of the host if namespaces are not mapped.
- **Frontend Container**: Executed as user `nginx` (UID 101). Inside Nginx, all processes are drop-jailed.
- **Backend Container**: Built using Java system users, running exclusively as user `spring` (UID 1000+).
- **Benefit**: If a vulnerability is compromised, the attacker cannot read system logs, alter package distributions, or modify configuration files within the container context.

### B. Fixed Container Image Strategy
We strictly avoid tags like `:latest` in production. `:latest` images are mutable and change over time. When a container restarts, it may pull an updated version containing breaking changes, leading to application crashes.
- **Nginx Proxy**: `nginx:1.25.4-alpine`
- **MySQL DB**: `mysql:8.0.36`
- **Java Platform**: `eclipse-temurin:17-jre-alpine`
- **Benefit**: Consistent, reliable deployments across local developer environments and cloud stages.

### C. Resource Contraints & Protection (`deploy.resources`)
Without resource limits, a memory leak in the backend or a massive DB query can consume all available system RAM, causing host-level out-of-memory (OOM) killer terminations that impact other critical applications.
- **Backend Memory Cap**: Limit set to `512MB` RAM and `0.5` CPU core.
- **Database Memory Cap**: Limit set to `512MB` RAM and `0.5` CPU core.
- **Benefit**: Limits container resource usage, ensuring high availability and protection against Denials of Service (DoS) attacks.

---

## 3. Database Backup Operations (`db_backup.sh`)

We ship a fully automated backup tool located at the root of the project: `db_backup.sh`.

### A. Backup Logic Details:
- Runs in non-interactive mode.
- Streams output securely using `mysqldump` without creating temporary files on host systems.
- Automatically saves outputs into `./backups/` directory on the host.
- Names backups using timestamp values: `backup_YYYY-MM-DD_HH-MM-SS.sql`.
- **Automatic retention cleaner**: Automatically purges any database backup file older than **7 days** to avoid disk space depletion.

### B. Execute Backup Manually:
Ensure the script is marked executable:
```bash
chmod +x db_backup.sh
./db_backup.sh
```

### C. Example Output Log:
```text
[+] Starting database backup process...
[+] Target Container: daw-mysql-db
[+] Target Database: daw_orchestration_db
[+] Backup Destination: ./backups/backup_2026-05-19_17-30-00.sql
[+] Backup completed successfully!
[+] File size: 2.1M
[+] Cleaning up backups older than 7 days...
[+] Database backup pipeline completed.
```

### D. Automated Cron Integration
To run this backup every night at 2:00 AM on a Unix system, append this line to `crontab -e`:
```text
0 2 * * * /bin/bash /DAW/trabajoRA3/container-orchestration/db_backup.sh >> /var/log/db_backup.log 2>&1
```

---

## 4. Disaster Recovery & Database Restoration

If the database gets corrupted or an admin deletes tables accidentally, follow these steps to restore from a backup:

### Step 1: Locate your backup file
Check the backups directory:
```bash
ls -la ./backups/
```
For example, let's use `backups/backup_2026-05-19_17-30-00.sql`.

### Step 2: Restore the SQL file into the container
Stream the SQL file back into the MySQL daemon via standard input:
```bash
docker exec -i daw-mysql-db mysql -u root -psupersecure_root_password_2026 daw_orchestration_db < ./backups/backup_2026-05-19_17-30-00.sql
```
*Note: Make sure to read passwords securely from `.env` in actual commands rather than hardcoding.*

### Step 3: Validate database data integrity
Trigger an immediate status update on the backend API to query the restored records:
```bash
curl -i http://localhost/api/status
```

---

## 5. Horizontal Scalability Recommendations

If web application traffic increases, Nginx can load balance requests across multiple Backend application instances.

### Scaling the Backend Tier:
Run this Docker Compose scaling command:
```bash
docker-compose up -d --scale backend=3
```
This launches 3 concurrent `backend` containers running side by side.

### Configuration updates:
To support scaling, the `nginx.conf` file needs to be updated with an `upstream` block listing the backend replicas:
```nginx
upstream backend_servers {
    server backend:8080;
}

server {
    ...
    location /api/ {
        proxy_pass http://backend_servers/api/;
    }
}
```
*Note: Docker DNS automatically resolves the `backend` domain to all active containers on the network in round-robin fashion, making scaling incredibly seamless!*
