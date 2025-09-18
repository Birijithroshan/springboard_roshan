-- Add missing columns to test_schedules table
-- Version 2.0 - Add notification features

ALTER TABLE test_schedules
ADD COLUMN notification_enabled BOOLEAN DEFAULT FALSE,
ADD COLUMN notification_emails VARCHAR(1000),
ADD COLUMN description TEXT;
