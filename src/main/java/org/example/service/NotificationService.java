package org.example.service;

import org.example.dto.TestExecutionResultDTO;
import org.example.model.TestSchedule;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void sendSuccessNotification(TestSchedule schedule, TestExecutionResultDTO result) {
        try {
            String subject = String.format("✅ Test Execution Success - %s", schedule.getName());
            String message = buildSuccessMessage(schedule, result);

            // In a real implementation, you would integrate with email service, Slack, etc.
            log.info("SUCCESS NOTIFICATION:\nSubject: {}\nMessage: {}", subject, message);

            // Simulate sending to configured emails
            if (schedule.getNotificationEmails() != null && !schedule.getNotificationEmails().isEmpty()) {
                String[] emails = schedule.getNotificationEmails().split(",");
                for (String email : emails) {
                    log.info("Sending success notification to: {}", email.trim());
                }
            }
        } catch (Exception e) {
            log.error("Error sending success notification", e);
        }
    }

    public void sendFailureNotification(TestSchedule schedule, String errorMessage) {
        try {
            String subject = String.format("❌ Test Execution Failed - %s", schedule.getName());
            String message = buildFailureMessage(schedule, errorMessage);

            log.error("FAILURE NOTIFICATION:\nSubject: {}\nMessage: {}", subject, message);

            // Simulate sending to configured emails
            if (schedule.getNotificationEmails() != null && !schedule.getNotificationEmails().isEmpty()) {
                String[] emails = schedule.getNotificationEmails().split(",");
                for (String email : emails) {
                    log.error("Sending failure notification to: {}", email.trim());
                }
            }
        } catch (Exception e) {
            log.error("Error sending failure notification", e);
        }
    }

    private String buildSuccessMessage(TestSchedule schedule, TestExecutionResultDTO result) {
        return String.format("""
            🎉 Test execution completed successfully!
            
            Schedule: %s
            Batch ID: %s
            Environment: %s
            Browser: %s
            
            📊 Results Summary:
            • Total Tests: %d
            • Passed: %d ✅
            • Failed: %d ❌
            • Skipped: %d ⏭️
            • Success Rate: %.1f%%
            
            ⏱️ Execution Details:
            • Start Time: %s
            • End Time: %s
            • Duration: %d seconds
            
            📋 Report: %s
            """,
            schedule.getName(),
            result.getBatchId(),
            result.getEnvironment(),
            result.getBrowser(),
            result.getTotalTests(),
            result.getPassedTests(),
            result.getFailedTests(),
            result.getSkippedTests(),
            calculateSuccessRate(result),
            result.getStartTime().format(formatter),
            result.getEndTime().format(formatter),
            result.getDuration() / 1000,
            result.getReportPath()
        );
    }

    private String buildFailureMessage(TestSchedule schedule, String errorMessage) {
        return String.format("""
            ⚠️ Test execution failed!
            
            Schedule: %s
            Environment: %s
            
            🔍 Error Details:
            %s
            
            🔄 Next scheduled execution: %s
            
            Please check the logs for more details.
            """,
            schedule.getName(),
            schedule.getEnvironment(),
            errorMessage,
            schedule.getNextExecution() != null ? schedule.getNextExecution().format(formatter) : "Not scheduled"
        );
    }

    private double calculateSuccessRate(TestExecutionResultDTO result) {
        if (result.getTotalTests() == 0) return 0.0;
        return (double) result.getPassedTests() / result.getTotalTests() * 100;
    }
}
