# Container Orchestration Project

This is a multi-tier web application environment created for the **"2º DAW - Despliegue de Aplicaciones Web"** course.

In this project, I have built a complete system using Docker, connecting different services together while keeping them secure and optimized.

## What I have done

- **Nginx Reverse Proxy:** I set up an Nginx container as the main entry point to route traffic to the frontend and backend.
- **Multi-Stage Docker Builds:** I created Dockerfiles for both React and Spring Boot that compile the code in one step and only copy the necessary files to the final image, making the images much smaller.
- **Network Separation:** I created two Docker networks (`frontend_net` and `backend_net`). The database is isolated in the backend network so it cannot be accessed directly from the internet.
- **Resource Limits:** I added CPU and memory limits to the containers in the `docker-compose.yml` to prevent them from using too many system resources.
- **Healthchecks:** I configured healthchecks so the containers wait for the database and backend to be fully ready before starting the next service.
- **Non-root Users:** I configured the containers to run as non-root users for better security.
- **Database Backups:** I included a simple script (`db_backup.sh`) to easily backup the MySQL database.

## System Architecture

- **Nginx Proxy:** Exposed on port 80. Routes `/` to the Frontend and `/api/` to the Backend.
- **React Frontend:** Runs on `frontend_net`.
- **Spring Boot Backend:** Runs on both `frontend_net` and `backend_net`.
- **MySQL Database:** Runs only on `backend_net` with a persistent volume.

## How to run the project

1. **Set up the environment variables:**
   Copy the `.env.example` file to a new file named `.env`:
   ```bash
   cp .env.example .env
   ```

2. **Start the containers:**
   You can use the provided script:
   ```bash
   ./start.sh
   ```
   Or run Docker Compose directly:
   ```bash
   docker-compose up -d
   ```

3. **Check the application:**
   - Frontend: `http://localhost`
   - Backend API: `http://localhost/api/status`

4. **Stop the containers:**
   Use the stop script:
   ```bash
   ./stop.sh
   ```
   Or run:
   ```bash
   docker-compose down
   ```

## More details

You can find more detailed manuals inside the `docs/` folder:
- `Deployment_Manual.md`: Details about the architecture and Docker configurations.
- `Administration_Manual.md`: How to manage the system and backups.
- `Testing_Manual.md`: How to test the connections and performance.
