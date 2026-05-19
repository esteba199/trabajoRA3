package com.daw.orchestration.repository;

import com.daw.orchestration.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Status Repository to perform JPA operations on 'service_status' table.
 */
@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    
    /**
     * Finds service status by service name.
     * @param serviceName Name of the service
     * @return Optional containing Status if found
     */
    Optional<Status> findByServiceName(String serviceName);
}
