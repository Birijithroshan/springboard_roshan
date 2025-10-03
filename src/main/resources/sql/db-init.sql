DROP TABLE IF EXISTS execution_log;

CREATE TABLE execution_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    test_name VARCHAR(255),
    status VARCHAR(20),
    test_type VARCHAR(50),
    us_id VARCHAR(50),
    tc_id VARCHAR(255),
    artifact LONGTEXT,
    screenshot_path VARCHAR(500),
    execution_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    suite_id INT,
    run_id INT,
    created_at DATETIME,
    start_time DATETIME,
    end_time DATETIME,
    duration VARCHAR(50)
);
