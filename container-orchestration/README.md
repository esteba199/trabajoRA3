# Optimized Container Orchestration for Web Applications

This project is a fully functional, production-ready, highly secure, and optimized multi-tier web application environment designed for the **"2º DAW - Despliegue de Aplicaciones Web"** curriculum.

The project demonstrates advanced DevOps concepts including:
- **Reverse Proxy Routing** using Nginx as a single entry point.
- **Multi-Stage Docker Builds** to minimize container image sizes and remove compile-time tools from runtime.
- **Strict Network Isolation** between public-facing and internal database tiers.
- **Resource Constraints** using CPU and Memory limits.
- **Self-Healing Infrastructure** through Docker healthchecks and startup synchronization (`depends_on: condition: service_healthy`).
- **Container Security** via non-root execution policies.
- **Infrastructure Maintenance** with automated db backups (`db_backup.sh`).

---

## 🏛️ System Architecture Diagram

```text
                             [ Client Browser ]
                                     │
                                     │ (Public Access: HTTP Port 80)
                                     ▼
                        ┌─────────────────────────┐
                        │          NGINX          │ (Public Entrypoint)
                        │   (Reverse Proxy Web)   │ [Image: alpine]
                        └────────────┬────────────┘
                                     │
                 ┌───────────────────┴───────────────────┐
                 │ (route: /)                            │ (route: /api/)
                 ▼                                       ▼
    ┌─────────────────────────┐             ┌─────────────────────────┐
    │     React Frontend      │             │    Spring Boot API      │
    │  [Image: alpine/nginx]  │             │   [Image: alpine/jre]   │
    │  Runs as user: nginx    │             │   Runs as user: spring  │
    └─────────────────────────┘             └────────────┬────────────┘
         (frontend_net)                                  │ (Internal 3306)
                                                         ▼
                                            ┌─────────────────────────┐
                                            │    MySQL Database       │
                                            │     [Image: mysql]      │
                                            │   Volume: mysql-data    │
                                            └─────────────────────────┘
                                                  (backend_net)
```

### Network Topology Boundaries
- **`frontend_net`**: Contains `nginx`, `frontend`, and `backend`. Nginx public traffic is limited to this network.
- **`backend_net`**: Contains `backend` and `mysql-db`. Completely isolated from the internet and the frontend container.

---

## 📂 Project Structure

```text
container-orchestration/
├── docker-compose.yml       # Production Docker Compose specification (v3.8)
├── .env                     # Local environment file containing database credentials
├── .env.example             # Template for local environment variables
├── .gitignore               # System, IDE, and build-output ignore configurations
├── db_backup.sh             # Script to backup the database using mysqldump
├── start.sh                 # Boot helper script
├── stop.sh                  # Tear-down helper script
├── rebuild.sh               # Cache-free rebuild utility
├── nginx/
│   ├── Dockerfile           # Alpine-based proxy Dockerfile
│   └── nginx.conf           # Proxy rules & security headers
├── frontend/
│   ├── Dockerfile           # Multi-stage React Node/Nginx builder
│   ├── nginx.conf           # Frontend Nginx config (Internal 8080)
│   ├── package.json         # React NPM configurations
│   ├── .dockerignore        # Build-context filtering
│   ├── public/              # Public asset template
│   └── src/                 # React UI code & CSS styling
├── backend/
│   ├── Dockerfile           # Multi-stage Maven/Temurin-JRE builder
│   ├── pom.xml              # Maven dependencies
│   ├── .dockerignore        # Build-context filtering
│   └── src/                 # Spring Boot source controllers & models
└── docs/                    # Technical manuals (Deployment, Admin, Testing)
    ├── Deployment_Manual.md
    ├── Administration_Manual.md
    └── Testing_Manual.md
```

---

## ⚡ Quick Start Guide

### Prerequisites
- Docker Engine installed and running.
- Docker Compose installed.
- (Optional) `bash` interpreter to run the backup and helper scripts.

### 1. Initialize Configuration
Copy the template `.env.example` file and create your custom `.env` config:
```bash
cp .env.example .env
```

### 2. Start the Environment
Run the startup helper script:
```bash
./start.sh
```
*Alternatively, run `docker-compose up -d`.*

The Docker engine will download base images, trigger the multi-stage compilations for React and Spring Boot, and deploy the entire multi-tier system.

### 3. Verify System Availability
Open your browser and navigate to:
- **Frontend Dashboard**: `http://localhost`
- **Backend Status Check**: `http://localhost/api/status`

### 4. Stopping the Environment
To safely shut down the containers while preserving database volume data:
```bash
./stop.sh
```

---

## 📘 Comprehensive Manuals

For in-depth analysis, setup guidelines, performance validation, and operations, please refer to the documents located in the `./docs/` directory:
1. **[Deployment Manual](file:///C:/DAW/trabajoRA3/container-orchestration/docs/Deployment_Manual.md)**: Thorough explanation of the architecture, networking, multi-stage builds, and deployment workflows.
2. **[Administration Manual](file:///C:/DAW/trabajoRA3/container-orchestration/docs/Administration_Manual.md)**: Details on running maintenance, security postures, backup plans (`db_backup.sh`), scaling parameters, and continuous upgrades.
3. **[Testing Manual](file:///C:/DAW/trabajoRA3/container-orchestration/docs/Testing_Manual.md)**: Methods to execute functional checks, verify network topology, run performance tests (ApacheBench), and analyze system logs.
