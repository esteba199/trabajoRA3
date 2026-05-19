package com.daw.orchestration.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Status Entity representing the database health verification table.
 * Mapped to 'service_status' table initialized in database/init.sql.
 */
@Entity
@Table(name = "service_status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "response_message", nullable = false, length = 255)
    private String responseMessage;

    @Column(name = "last_verified")
    private LocalDateTime lastVerified;

    // Constructors
    public Status() {
    }

    public Status(String serviceName, String status, String responseMessage, LocalDateTime lastVerified) {
        this.serviceName = serviceName;
        this.status = status;
        this.responseMessage = responseMessage;
        this.lastVerified = lastVerified;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public LocalDateTime getLastVerified() {
        return lastVerified;
    }

    public void setLastVerified(LocalDateTime lastVerified) {
        this.lastVerified = lastVerified;
    }
}
