---
title: "RA3 Practice - Container Orchestration with Docker Compose"
author: "Student"
date: "`r Sys.Date()`"
output: html_document
---

# Introduction

In this practice, a multi-container application was deployed using Docker Compose. The architecture is composed of the following services:

* React frontend
* Spring Boot backend
* MySQL database
* Nginx reverse proxy

The main objective was to validate service communication, security, network isolation, container management, and performance within a Docker environment.

# Application Deployment

The environment was started using the following command:

```bash
docker compose up --build
```

This command builds the required Docker images and starts all services defined in the `docker-compose.yml` file.

The status of the containers was verified with:

```bash
docker compose ps
```

All containers were running successfully, and the MySQL service reported a `healthy` status.

# Functional Verification

The frontend application was accessed through:

```text
http://localhost
```

The React interface loaded correctly through the Nginx reverse proxy.

The backend API was tested using:

```bash
curl -i http://localhost/api/status
```

The JSON response confirmed successful communication between Nginx, the Spring Boot backend, and the MySQL database.

# Security and Network Isolation

Network isolation was tested to line ensure proper security between services.

The frontend container was unable to communicate directly with the database container, while the backend container successfully connected to it. This confirms that Docker networks were correctly segmented and that access to the database is restricted to authorized services only.

Additionally, it was verified that only the Nginx container exposes a port to the host machine, while the remaining services remain internal.

# Health Checks

A health check was configured for the MySQL service.

The container status was inspected and returned:

```text
healthy
```

This confirms that the database was fully initialized and available before backend connections were established.

# Logs and Monitoring

Container logs were reviewed using:

```bash
docker logs <container_name>
```

The backend logs contained the message:

```text
Started OrchestrationApplication
```

indicating that the Spring Boot application started successfully.

The MySQL logs also confirmed the execution of the initialization script (`init.sql`) during startup.

# Performance Testing

A load test was executed using Apache Bench:

```bash
ab -n 1000 -c 50 http://localhost/api/status
```

The test generated 1000 requests with 50 concurrent connections.

During execution, container resource usage was monitored with:

```bash
docker stats
```

The results demonstrated that the application handled concurrent requests correctly while respecting the configured CPU and memory limits.

# Backup Procedure

The project includes a backup script named `db_backup.sh`, designed to automate MySQL database backups.

The script generates SQL backup files that can be used to restore the database if necessary. This provides an additional layer of reliability and data protection.

# Multi-Stage Build Optimization

Docker images were inspected after the build process.

The project uses multi-stage builds to reduce image size by excluding unnecessary build dependencies from the final containers.

This approach improves deployment efficiency and reduces storage requirements.

# Conclusion

This practice successfully demonstrated the deployment of a multi-container architecture using Docker Compose.

The implementation validated:

* Service communication between containers.
* Reverse proxy functionality using Nginx.
* Network isolation and security.
* Database health checks.
* Log monitoring and verification.
* Performance testing under load.
* Backup automation.
* Multi-stage image optimization.

Overall, the project meets the requirements of container orchestration and demonstrates the effective use of Docker technologies in a distributed application environment.
