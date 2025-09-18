package org.example.service;

import org.example.dto.TestExecutionResultDTO;
import org.example.model.TestSchedule;
import org.example.repository.TestScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ScheduledTestExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTestExecutionService.class);

    @Autowired
    private TestScheduleRepository scheduleRepository;

    @Autowired
    private TestExecutionService executionService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Check for scheduled tests every minute
     */
    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void checkScheduledTests() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<TestSchedule> dueSchedules = scheduleRepository.findDueSchedules(now);

            log.info("Found {} scheduled tests due for execution", dueSchedules.size());

            for (TestSchedule schedule : dueSchedules) {
                if (schedule.getIsActive()) {
                    executeScheduledTest(schedule);
                }
            }
        } catch (Exception e) {
            log.error("Error checking scheduled tests", e);
        }
    }

    /**
     * Daily cleanup of old execution records
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    public void dailyCleanup() {
        try {
            log.info("Starting daily cleanup of old execution records");

            // Clean up executions older than 30 days
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int deletedCount = executionService.cleanupOldExecutions(cutoffDate);

            log.info("Daily cleanup completed. Deleted {} old execution records", deletedCount);
        } catch (Exception e) {
            log.error("Error during daily cleanup", e);
        }
    }

    /**
     * Weekly report generation
     */
    @Scheduled(cron = "0 0 8 * * MON") // Run at 8 AM every Monday
    public void weeklyReportGeneration() {
        try {
            log.info("Starting weekly report generation");

            LocalDateTime weekStart = LocalDateTime.now().minusWeeks(1);
            LocalDateTime weekEnd = LocalDateTime.now();

            // Generate summary report for the week
            CompletableFuture.runAsync(() -> {
                try {
                    executionService.generateWeeklySummaryReport(weekStart, weekEnd);
                    log.info("Weekly report generation completed");
                } catch (Exception e) {
                    log.error("Error generating weekly report", e);
                }
            });

        } catch (Exception e) {
            log.error("Error starting weekly report generation", e);
        }
    }

    private void executeScheduledTest(TestSchedule schedule) {
        try {
            log.info("Executing scheduled test: {}", schedule.getName());

            // Update next execution time
            schedule.setLastExecution(LocalDateTime.now());
            schedule.setNextExecution(calculateNextExecution(schedule));
            scheduleRepository.save(schedule);

            // Execute tests asynchronously
            CompletableFuture<TestExecutionResultDTO> future = executionService.executeScheduledTests(schedule);

            // Handle completion
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Scheduled test execution failed for: " + schedule.getName(), throwable);
                    if (schedule.getNotificationEnabled()) {
                        notificationService.sendFailureNotification(schedule, throwable.getMessage());
                    }
                } else {
                    log.info("Scheduled test execution completed for: {} with status: {}",
                        schedule.getName(), result.getStatus());
                    if (schedule.getNotificationEnabled()) {
                        notificationService.sendSuccessNotification(schedule, result);
                    }
                }
            });

        } catch (Exception e) {
            log.error("Error executing scheduled test: " + schedule.getName(), e);
        }
    }

    private LocalDateTime calculateNextExecution(TestSchedule schedule) {
        // This is a simplified implementation
        // In a real scenario, you'd use a proper cron expression parser
        try {
            String cronExpression = schedule.getCronExpression();

            // For demo purposes, assume simple patterns
            if (cronExpression.contains("0 0 * * * ?")) { // Daily
                return schedule.getLastExecution().plusDays(1);
            } else if (cronExpression.contains("0 0 * * * MON")) { // Weekly
                return schedule.getLastExecution().plusWeeks(1);
            } else if (cronExpression.contains("0 0 1 * * ?")) { // Monthly
                return schedule.getLastExecution().plusMonths(1);
            } else {
                // Default to 1 hour from now
                return LocalDateTime.now().plusHours(1);
            }
        } catch (Exception e) {
            log.error("Error calculating next execution time for schedule: " + schedule.getName(), e);
            return LocalDateTime.now().plusHours(1);
        }
    }
}
