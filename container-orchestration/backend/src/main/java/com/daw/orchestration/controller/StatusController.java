package com.daw.orchestration.controller;

import com.daw.orchestration.entity.Status;
import com.daw.orchestration.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * StatusController exposes /api/status endpoint.
 * This endpoint verifies the communication between the Backend and MySQL.
 */
@RestController
@RequestMapping("/status")
public class StatusController {

    private final StatusService statusService;

    @Autowired
    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    /**
     * Exposes status check. Retrieves database connection status.
     * @return ResponseEntity with structured JSON payload representing stack status
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("version", "1.0.0");
        response.put("framework", "Spring Boot 3.2.4");
        
        Optional<Status> dbStatus = statusService.getDatabaseStatus();
        
        if (dbStatus.isPresent()) {
            response.put("databaseConnected", true);
            response.put("dbName", "MySQL Database v8.0");
            response.put("dbStatus", dbStatus.get());
        } else {
            response.put("databaseConnected", false);
            response.put("dbName", "MySQL Database");
            response.put("dbStatus", null);
            response.put("error", "Could not establish database read/write session.");
        }

        return ResponseEntity.ok(response);
    }
}
