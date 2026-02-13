-- CivicPulse Database Schema
-- MySQL 8.0+

-- Create database
CREATE DATABASE IF NOT EXISTS civicpulse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE civicpulse;

-- Drop existing tables (for clean setup)
DROP TABLE IF EXISTS issue_timeline;
DROP TABLE IF EXISTS citizen_issue_map;
DROP TABLE IF EXISTS issues;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS users;

-- Users Table
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CITIZEN', 'ADMIN', 'DEPARTMENT') DEFAULT 'CITIZEN',
    dept_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Departments Table
CREATE TABLE departments (
    dept_id INT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(50) NOT NULL,
    dept_type ENUM('ROAD', 'WATER', 'SANITATION', 'ELECTRICITY') NOT NULL,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE KEY unique_dept_type (dept_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Issues Table
CREATE TABLE issues (
    issue_id VARCHAR(30) PRIMARY KEY,
    category ENUM('ROAD', 'WATER', 'SANITATION', 'ELECTRICITY') NOT NULL,
    location_lat DECIMAL(10, 8) NOT NULL,
    location_lng DECIMAL(11, 8) NOT NULL,
    address TEXT NOT NULL,
    description TEXT NOT NULL,
    status ENUM('REPORTED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED') DEFAULT 'REPORTED',
    report_count INT DEFAULT 1,
    dept_id INT NULL,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE SET NULL,
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_location (location_lat, location_lng),
    INDEX idx_created_at (created_at),
    INDEX idx_category_status (category, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Citizen Issue Mapping Table
CREATE TABLE citizen_issue_map (
    map_id INT PRIMARY KEY AUTO_INCREMENT,
    citizen_id INT NOT NULL,
    issue_id VARCHAR(30) NOT NULL,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (citizen_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (issue_id) REFERENCES issues(issue_id) ON DELETE CASCADE,
    UNIQUE KEY unique_citizen_issue (citizen_id, issue_id),
    INDEX idx_citizen (citizen_id),
    INDEX idx_issue (issue_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Issue Timeline Table
CREATE TABLE issue_timeline (
    timeline_id INT PRIMARY KEY AUTO_INCREMENT,
    issue_id VARCHAR(30) NOT NULL,
    status ENUM('REPORTED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED') NOT NULL,
    updated_by INT NOT NULL,
    remarks TEXT,
    proof_image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (issue_id) REFERENCES issues(issue_id) ON DELETE CASCADE,
    FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_issue (issue_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert Default Departments
INSERT INTO departments (dept_name, dept_type, contact_email, contact_phone) VALUES
('Road Department', 'ROAD', 'road@civicpulse.com', '1800-111-001'),
('Water Department', 'WATER', 'water@civicpulse.com', '1800-111-002'),
('Sanitation Department', 'SANITATION', 'sanitation@civicpulse.com', '1800-111-003'),
('Electricity Department', 'ELECTRICITY', 'electricity@civicpulse.com', '1800-111-004');

-- Insert Default Admin User
-- Password: admin123 (hashed with BCrypt)
INSERT INTO users (name, email, phone, password_hash, role, dept_id) VALUES
('System Admin', 'admin@civicpulse.com', '9999999999', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYCj.1NqYGa', 'ADMIN', NULL);

-- Insert Department Users
-- Password: dept123 (hashed with BCrypt)
INSERT INTO users (name, email, phone, password_hash, role, dept_id) VALUES
('Road Department User', 'road@civicpulse.com', '9999999001', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYCj.1NqYGa', 'DEPARTMENT', 1),
('Water Department User', 'water@civicpulse.com', '9999999002', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYCj.1NqYGa', 'DEPARTMENT', 2),
('Sanitation Department User', 'sanitation@civicpulse.com', '9999999003', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYCj.1NqYGa', 'DEPARTMENT', 3),
('Electricity Department User', 'electricity@civicpulse.com', '9999999004', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYCj.1NqYGa', 'DEPARTMENT', 4);

-- Add foreign key constraint for users.dept_id
ALTER TABLE users ADD CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE SET NULL;

-- Create view for issue statistics
CREATE OR REPLACE VIEW issue_stats AS
SELECT 
    category,
    status,
    COUNT(*) as count,
    AVG(report_count) as avg_reports
FROM issues
GROUP BY category, status;

-- Create view for department performance
CREATE OR REPLACE VIEW department_performance AS
SELECT 
    d.dept_name,
    d.dept_type,
    COUNT(i.issue_id) as total_issues,
    SUM(CASE WHEN i.status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved_issues,
    SUM(CASE WHEN i.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_issues,
    AVG(CASE 
        WHEN i.status = 'RESOLVED' AND i.resolved_at IS NOT NULL 
        THEN TIMESTAMPDIFF(HOUR, i.created_at, i.resolved_at) 
        ELSE NULL 
    END) as avg_resolution_hours
FROM departments d
LEFT JOIN issues i ON d.dept_id = i.dept_id
GROUP BY d.dept_id, d.dept_name, d.dept_type;

COMMIT;
