-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS automation_tests;

-- Use the database
USE automation_tests;

-- Drop the table if it exists (for clean recreation)
DROP TABLE IF EXISTS execution_log;

-- Create execution_log table
CREATE TABLE execution_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    test_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    test_type VARCHAR(50) NOT NULL,
    us_id VARCHAR(50),
    tc_id VARCHAR(255),
    artifact VARCHAR(255),
    screenshot_path VARCHAR(255),
    execution_time DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create an alias view for the plural version just in case
CREATE OR REPLACE VIEW execution_logs AS SELECT * FROM execution_log;

-- Grant permissions
GRANT ALL PRIVILEGES ON automation_tests.* TO 'root'@'localhost';
FLUSH PRIVILEGES;

-- Show tables to confirm creation
SHOW TABLES;
