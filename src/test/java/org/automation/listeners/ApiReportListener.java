package org.automation.listeners;

import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ITestContext;
import org.automation.utils.ReportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TestNG listener specifically for API test reporting and logging
 */
public class ApiReportListener implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(ApiReportListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.info("Starting API test: {}", testName);

        // Set start time for API test tracking
        System.setProperty("api.test.start." + testName, String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();

        logger.info("API test PASSED: {} (Duration: {}ms)", testName, duration);

        // Log API test details
        logApiTestResult(result, "PASSED", duration);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();
        String errorMessage = result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown API error";

        logger.error("API test FAILED: {} (Duration: {}ms) - Error: {}", testName, duration, errorMessage);

        // Log API test details with failure information
        logApiTestResult(result, "FAILED", duration);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String skipReason = result.getThrowable() != null ? result.getThrowable().getMessage() : "API test skipped";

        logger.warn("API test SKIPPED: {} - Reason: {}", testName, skipReason);

        // Log API test details for skipped test
        logApiTestResult(result, "SKIPPED", 0L);
    }

    @Override
    public void onFinish(ITestContext context) {
        // Generate API test summary report
        try {
            ReportUtils.generateApiTestSummary(context);
            logger.info("API test summary report generated for suite: {}", context.getSuite().getName());
        } catch (Exception e) {
            logger.error("Failed to generate API test summary: {}", e.getMessage());
        }
    }

    /**
     * Log API test result details to artifacts
     */
    private void logApiTestResult(ITestResult result, String status, long duration) {
        try {
            String testName = result.getMethod().getMethodName();
            String className = result.getTestClass().getName();

            // Create API test result JSON
            String apiResultJson = String.format("""
                {
                    "testName": "%s",
                    "className": "%s",
                    "status": "%s",
                    "duration": %d,
                    "timestamp": "%s",
                    "error": "%s"
                }
                """,
                testName,
                className,
                status,
                duration,
                java.time.LocalDateTime.now().toString(),
                result.getThrowable() != null ? result.getThrowable().getMessage().replace("\"", "'") : ""
            );

            // Save to artifacts/api directory
            ReportUtils.saveApiTestResult(testName, apiResultJson);

        } catch (Exception e) {
            logger.warn("Failed to log API test result: {}", e.getMessage());
        }
    }
}
