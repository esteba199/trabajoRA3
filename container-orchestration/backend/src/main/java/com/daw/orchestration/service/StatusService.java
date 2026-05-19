package com.daw.orchestration.service;

import com.daw.orchestration.entity.Status;
import com.daw.orchestration.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service to execute business logic related to checking database connectivity.
 */
@Service
public class StatusService {

    private final StatusRepository statusRepository;

    @Autowired
    public StatusService(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    /**
     * Checks database connection status. Queries the seed data to confirm connectivity.
     * Updates the last_verified timestamp.
     * @return Optional of Status
     */
    @Transactional
    public Optional<Status> getDatabaseStatus() {
        try {
            Optional<Status> dbStatus = statusRepository.findByServiceName("MySQL Database");
            if (dbStatus.isPresent()) {
                Status status = dbStatus.get();
                status.setLastVerified(LocalDateTime.now());
                // Save and flush changes to DB to prove write connectivity works as well
                return Optional.of(statusRepository.saveAndFlush(status));
            }
            return Optional.empty();
        } catch (Exception e) {
            // Logs would show the specific error (e.g. connection lost)
            System.err.println("Database connectivity error: " + e.getMessage());
            return Optional.empty();
        }
    }
}
