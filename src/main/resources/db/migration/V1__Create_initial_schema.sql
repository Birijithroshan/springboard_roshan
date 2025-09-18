-- Create initial database schema for automation framework
-- Version 1.0 - Initial schema creation

-- Create test_batches table
CREATE TABLE test_batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    total_tests INT DEFAULT 0,
    passed_tests INT DEFAULT 0,
    failed_tests INT DEFAULT 0,
    skipped_tests INT DEFAULT 0,
    environment VARCHAR(100),
    created_by VARCHAR(100),
    INDEX idx_batch_id (batch_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Create test_cases table
CREATE TABLE test_cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    test_type ENUM('UI', 'API', 'INTEGRATION', 'UNIT') NOT NULL,
    category VARCHAR(100),
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    is_active BOOLEAN DEFAULT TRUE,
    test_data JSON,
    expected_result TEXT,
    tags VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    INDEX idx_test_type (test_type),
    INDEX idx_category (category),
    INDEX idx_priority (priority),
    INDEX idx_is_active (is_active)
);

-- Create test_executions table
CREATE TABLE test_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_case_id BIGINT NOT NULL,
    test_batch_id BIGINT,
    execution_id VARCHAR(255) NOT NULL,
    status ENUM('PENDING', 'RUNNING', 'PASSED', 'FAILED', 'SKIPPED', 'ERROR') NOT NULL,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    execution_duration BIGINT, -- in milliseconds
    error_message TEXT,
    stack_trace TEXT,
    screenshot_path VARCHAR(500),
    log_file_path VARCHAR(500),
    environment VARCHAR(100),
    browser VARCHAR(50),
    executed_by VARCHAR(100),
    actual_result TEXT,
    retry_count INT DEFAULT 0,
    thread_id VARCHAR(100),
    FOREIGN KEY (test_case_id) REFERENCES test_cases(id) ON DELETE CASCADE,
    FOREIGN KEY (test_batch_id) REFERENCES test_batches(id) ON DELETE SET NULL,
    INDEX idx_execution_id (execution_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_test_case_id (test_case_id),
    INDEX idx_test_batch_id (test_batch_id)
);

-- Create test_schedules table
CREATE TABLE test_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    cron_expression VARCHAR(100) NOT NULL,
    test_case_ids JSON,
    is_active BOOLEAN DEFAULT TRUE,
    environment VARCHAR(100),
    browser VARCHAR(50),
    parallel_threads INT DEFAULT 1,
    timeout_minutes INT DEFAULT 60,
    retry_enabled BOOLEAN DEFAULT FALSE,
    max_retries INT DEFAULT 0,
    notification_enabled BOOLEAN DEFAULT FALSE,
    notification_emails VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_execution TIMESTAMP NULL,
    next_execution TIMESTAMP NULL,
    created_by VARCHAR(100),
    INDEX idx_is_active (is_active),
    INDEX idx_next_execution (next_execution)
);

-- Create test_reports table
CREATE TABLE test_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id VARCHAR(255) NOT NULL,
    report_type ENUM('HTML', 'CSV', 'XML', 'JSON') NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_tests INT DEFAULT 0,
    passed_tests INT DEFAULT 0,
    failed_tests INT DEFAULT 0,
    skipped_tests INT DEFAULT 0,
    execution_duration BIGINT, -- in milliseconds
    FOREIGN KEY (batch_id) REFERENCES test_batches(batch_id) ON DELETE CASCADE,
    INDEX idx_batch_id (batch_id),
    INDEX idx_report_type (report_type),
    INDEX idx_generated_at (generated_at)
);

-- Create test_screenshots table
CREATE TABLE test_screenshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_execution_id BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    screenshot_type ENUM('SUCCESS', 'FAILURE', 'STEP') NOT NULL,
    step_description TEXT,
    captured_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_size BIGINT,
    FOREIGN KEY (test_execution_id) REFERENCES test_executions(id) ON DELETE CASCADE,
    INDEX idx_test_execution_id (test_execution_id),
    INDEX idx_screenshot_type (screenshot_type)
);

-- Create test_logs table
CREATE TABLE test_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_execution_id BIGINT NOT NULL,
    log_level ENUM('TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL') NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logger_name VARCHAR(255),
    thread_name VARCHAR(100),
    exception_trace TEXT,
    FOREIGN KEY (test_execution_id) REFERENCES test_executions(id) ON DELETE CASCADE,
    INDEX idx_test_execution_id (test_execution_id),
    INDEX idx_log_level (log_level),
    INDEX idx_timestamp (timestamp)
);
