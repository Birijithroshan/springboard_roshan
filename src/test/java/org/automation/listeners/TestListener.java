package org.automation.listeners;

import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ITestContext;
import org.automation.utils.ScreenshotUtils;
import org.automation.utils.DatabaseInserter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main TestNG listener for handling test execution events
 */
public class TestListener implements ITestListener {
    
    private static final Logger logger = LoggerFactory.getLogger(TestListener.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        logger.info("Starting test: {}.{}", className, testName);
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        long duration = result.getEndMillis() - result.getStartMillis();
        
        logger.info("Test PASSED: {}.{} (Duration: {}ms)", className, testName, duration);
        
        // Log to database
        try {
            DatabaseInserter.insertTestResult(
                className,
                testName,
                "PASSED",
                LocalDateTime.now().format(formatter),
                duration,
                null,
                null
            );
        } catch (Exception e) {
            logger.warn("Failed to log test result to database: {}", e.getMessage());
        }
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        long duration = result.getEndMillis() - result.getStartMillis();
        String errorMessage = result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown error";
        
        logger.error("Test FAILED: {}.{} (Duration: {}ms) - Error: {}", 
                    className, testName, duration, errorMessage);
        
        // Take screenshot for UI tests
        String screenshotPath = null;
        if (className.contains("ui") || className.contains("UI")) {
            screenshotPath = ScreenshotUtils.takeScreenshot(testName + "_FAILED");
            logger.info("Screenshot captured: {}", screenshotPath);
        }
        
        // Log to database
        try {
            DatabaseInserter.insertTestResult(
                className,
                testName,
                "FAILED",
                LocalDateTime.now().format(formatter),
                duration,
                errorMessage,
                screenshotPath
            );
        } catch (Exception e) {
            logger.warn("Failed to log test result to database: {}", e.getMessage());
        }
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        String skipReason = result.getThrowable() != null ? result.getThrowable().getMessage() : "Test skipped";
        
        logger.warn("Test SKIPPED: {}.{} - Reason: {}", className, testName, skipReason);
        
        // Log to database
        try {
            DatabaseInserter.insertTestResult(
                className,
                testName,
                "SKIPPED",
                LocalDateTime.now().format(formatter),
                0L,
                skipReason,
                null
            );
        } catch (Exception e) {
            logger.warn("Failed to log test result to database: {}", e.getMessage());
        }
    }
    
    @Override
    public void onStart(ITestContext context) {
        logger.info("Starting test suite: {}", context.getSuite().getName());
    }
    
    @Override
    public void onFinish(ITestContext context) {
        int total = context.getAllTestMethods().length;
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        
        logger.info("Test suite completed: {} - Total: {}, Passed: {}, Failed: {}, Skipped: {}",
                   context.getSuite().getName(), total, passed, failed, skipped);
    }
}
