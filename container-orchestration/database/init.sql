-- ==============================================================================
-- Database Initialization Script for 2º DAW Container Orchestration
-- ==============================================================================

USE daw_orchestration_db;

-- Create status table to store service health information
CREATE TABLE IF NOT EXISTS service_status (
    id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    response_message VARCHAR(255) NOT NULL,
    last_verified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Populate table with initial values
INSERT INTO service_status (service_name, status, response_message) 
VALUES 
('MySQL Database', 'HEALTHY', 'Database connection successfully established and initial seed data fetched.');
