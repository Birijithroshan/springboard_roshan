-- Update test_cases table to include new enum values
-- Version 3.0 - Update enums and add missing columns

-- Update test_type enum to include all required values
ALTER TABLE test_cases MODIFY COLUMN test_type ENUM('WEB_UI', 'UI', 'API', 'DATABASE', 'INTEGRATION') NOT NULL;

-- Add missing columns if they don't exist
ALTER TABLE test_cases
ADD COLUMN IF NOT EXISTS test_suite VARCHAR(255),
ADD COLUMN IF NOT EXISTS environment VARCHAR(100);

-- Ensure category and created_by columns exist
ALTER TABLE test_cases
ADD COLUMN IF NOT EXISTS category VARCHAR(100),
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
