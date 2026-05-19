package com.daw.orchestration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class to bootstrap and run the Spring Boot REST API service.
 * Part of 2º DAW - Despliegue de Aplicaciones Web University Project.
 */
@SpringBootApplication
public class OrchestrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestrationApplication.class, args);
    }
}
