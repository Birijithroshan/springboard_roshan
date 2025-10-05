package org.automation.listeners;

import org.testng.ITestListener;
import org.testng.ITestResult;
import org.automation.utils.ScreenshotUtils;
import org.automation.ui.DriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TestNG listener specifically for handling screenshot capture on test failures
 */
public class ScreenshotListener implements ITestListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotListener.class);
    
    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        
        // Only take screenshots for UI tests
        if (isUiTest(className)) {
            try {
                String screenshotPath = ScreenshotUtils.takeScreenshot(testName + "_FAILED");
                if (screenshotPath != null) {
                    logger.info("Screenshot captured for failed test {}: {}", testName, screenshotPath);
                    
                    // Attach screenshot path to test result for reporting
                    System.setProperty("screenshot.path." + testName, screenshotPath);
                } else {
                    logger.warn("Failed to capture screenshot for test: {}", testName);
                }
            } catch (Exception e) {
                logger.error("Error capturing screenshot for test {}: {}", testName, e.getMessage());
            }
        }
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        
        // Optionally capture success screenshots for UI tests in certain scenarios
        boolean captureSuccessScreenshots = Boolean.parseBoolean(
            System.getProperty("capture.success.screenshots", "false")
        );
        
        if (isUiTest(className) && captureSuccessScreenshots) {
            try {
                String screenshotPath = ScreenshotUtils.takeScreenshot(testName + "_SUCCESS");
                if (screenshotPath != null) {
                    logger.debug("Success screenshot captured for test {}: {}", testName, screenshotPath);
                    System.setProperty("screenshot.path." + testName, screenshotPath);
                }
            } catch (Exception e) {
                logger.debug("Error capturing success screenshot for test {}: {}", testName, e.getMessage());
            }
        }
    }
    
    /**
     * Determines if the test is a UI test based on class name or package structure
     */
    private boolean isUiTest(String className) {
        return className.contains(".ui.") || 
               className.contains("UI") || 
               className.contains("Selenium") ||
               className.contains("Web") ||
               className.toLowerCase().contains("browser");
    }
}
