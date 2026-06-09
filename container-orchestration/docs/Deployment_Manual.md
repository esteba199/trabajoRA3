# Deployment Manual
## Optimized Container Orchestration for Web Applications

This document provides complete instructions for compiling, deploying, and validating the multi-tier containerized web application environment for **2º DAW - Despliegue de Aplicaciones Web**.

---

## 1. Architectural Architecture & Role of Services

This project implements a standard **Production-Grade Three-Tier Architecture** decoupled and managed via isolated Docker containers:

| Service | Technology | Role | Network Access | Image Strategy |
| :--- | :--- | :--- | :--- | :--- |
| **Nginx Proxy** | Nginx | **Reverse Proxy & Gateway**. Handles routing, SSL termination (optional), and public HTTP entry. | Exposed publicly on port `80`. Communicates internally with Frontend and Backend. | `nginx:1.25.4-alpine` |
| **Frontend** | React | **Presentation Tier**. Serves the static HTML/JS/CSS assets to client browsers. | Internal port `8080` (accessible only from the Nginx proxy via Docker network). | Custom multi-stage build running on Alpine Nginx as user `nginx`. |
| **Backend** | Spring Boot (Java) | **Logic Tier**. Exposes REST API endpoints (`/api/*`) for data processing. | Internal port `8080` (accessible from Nginx and MySQL). No host ports exposed. | Custom multi-stage build running Eclipse Temurin JRE as user `spring`. |
| **MySQL DB** | MySQL | **Database Tier**. Relational persistence store. | Internal port `3306` (accessible only from the Spring Boot API). | Official `mysql:8.0.36` with persistent volume mount. |

---

## 2. Docker Networking & Security Isolation

Container security is heavily enforced via network layer boundaries:

```text
                     [ Internet Client ]
                             │
                             ▼ (Exposed port 80)
                     ┌───────────────┐
                     │  Nginx Proxy  │
                     └───────┬───────┘
                             │
            ┌────────────────┴────────────────┐
            │  (frontend_net bridge)          │  (frontend_net bridge)
            ▼                                 ▼
┌───────────────────────┐         ┌───────────────────────┐
│   React Frontend      │         │   Spring Boot API     │
│   (Port 8080)         │         │   (Port 8080)         │
└───────────────────────┘         └───────────┬───────────┘
                                              │
                                              │  (backend_net bridge)
                                              ▼
                                  ┌───────────────────────┐
                                  │   MySQL Database      │
                                  │   (Port 3306)         │
                                  └───────────────────────┘
```

### Purpose of Network Isolation:
1. **Frontend Isolation**: The React Frontend container serves only static static assets. It has no reason to connect to the SQL database. By keeping it on `frontend_net` and excluding it from `backend_net`, we guarantee that even if the frontend container is compromised, the attacker has *zero* network access to the database.
2. **Database Isolation**: The MySQL container is placed exclusively on `backend_net`. It does not expose any port to the host system and is invisible to the frontend container. Only the backend API can communicate with it.
3. **No Direct Host Access**: All internal communications bypass the host machine's routing, preventing port exposure conflicts and blocking external database inspection attempts.

---

## 3. Multi-Stage Builds: Optimization & Security Benefits

Both Frontend and Backend use **Multi-Stage Dockerfiles** to achieve the highest level of container optimization:

### A. React Frontend (`frontend/Dockerfile`)
- **Stage 1 (Build)**: Pulls `node:18-alpine` (approx. 200MB). Installs compilers and packages, and generates static assets using `npm run build`.
- **Stage 2 (Run)**: Spins up a clean `nginx:1.25.4-alpine` instance (approx. 23MB). Copies *only* the compiled static directory `/app/build` into Nginx's web directory and discards the Node runtime environment.
- **Benefits**: Discards all developmental Node packages (Webpack, linters, source maps) resulting in a lightweight, rapid-loading image with a drastically reduced attack surface.

### B. Spring Boot Backend (`backend/Dockerfile`)
- **Stage 1 (Build)**: Pulls a full JDK environment `maven:3.8.5-openjdk-17-slim` (approx. 400MB) to pull dependencies and package the project into a Fat JAR (`mvn clean package`).
- **Stage 2 (Run)**: Employs a hardened `eclipse-temurin:17-jre-alpine` runtime image (approx. 150MB). Copies *only* the single production `.jar` and drops the Maven compiler, build configuration files, and JDK tools.
- **Benefits**: Maven build utilities are excluded from the final execution image. In the event of a container breach, an attacker has no compilation tools to construct or run arbitrary binary payloads inside the server environment.

---

## 4. Healthcheck and Initialization Startup Flow

A frequent issue in multi-tier applications is **Startup Race Conditions**. The Backend container often launches and attempts to establish a connection to MySQL *before* the SQL daemon is fully initialized and accepting sockets, leading to app crashes.

This project implements a double-layered boot guard:

### 1. Database Health Check:
The `mysql-db` container runs a persistent status ping:
```yaml
healthcheck:
  test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
  interval: 10s
  timeout: 5s
  retries: 5
  start_period: 15s
```

### 2. Startup Synchronization:
The `backend` container states an explicit dependency on database health:
```yaml
depends_on:
  mysql-db:
    condition: service_healthy
```
This forces Docker Compose to hold off on spinning up the `backend` process until the database health check reports `healthy` (i.e. MySQL database is fully operational and initialized).

---

## 5. Deployment Procedures

### Step 1: Clone and Configure Environment
Verify the directory contents are present:
```bash
cd c/DAW/trabajoRA3/container-orchestration
```

Copy the `.env.example` configurations into `.env` and alter database secrets:
```bash
cp .env.example .env
```

### Step 2: Build and Deploy Tiers
Initialize the Docker Compose stack using the start script:
```bash
./start.sh
```
*Behind the scenes:* This runs `docker-compose up -d`. The engine compiles the React assets, downloads MySQL/Nginx binaries, compiles the Spring JAR, creates volumes, sets bridges, and spins up the stack.

### Step 3: Inspect Deployment Progress
To watch the orchestration build process and monitor active services:
```bash
docker-compose ps
```

To view live container logs:
```bash
docker-compose logs -f
```

---

## 6. Maintenance Commands (Start, Stop, Update)

### Start Services
```bash
./start.sh
```

### Stop Services (Preserving Database Volume Data)
```bash
./stop.sh
```

### Wipe Data Volume and Reset Containers
```bash
docker-compose down -v
```

### Rebuild from Scratch (Clear Caches)
```bash
./rebuild.sh
```
*(Executes a complete fresh compilation by forcing docker-compose build with `--no-cache`)*
