package org.example.engine;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Parameters;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for UI tests with WebDriver setup and utilities
 */
public class BaseUITest {

    protected WebDriver driver;
    protected Logger testLogger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WebDriverManager webDriverManager;

    @BeforeMethod
    @Parameters({"browser"})
    public void setUp(String browser) {
        try {
            if (webDriverManager != null) {
                driver = webDriverManager.createDriver(browser != null ? browser : "chrome", false);
            } else {
                // Fallback to manual WebDriver creation if autowiring fails
                driver = createFallbackDriver();
            }
            testLogger.info("Starting UI test: " + this.getClass().getSimpleName() + " with browser: " + browser);
        } catch (Exception e) {
            testLogger.error("Failed to set up WebDriver", e);
            driver = createFallbackDriver();
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
                testLogger.info("Completed UI test: " + this.getClass().getSimpleName());
            } catch (Exception e) {
                testLogger.error("Error during WebDriver cleanup", e);
            }
        }
    }

    private WebDriver createFallbackDriver() {
        try {
            // Use WebDriverManager utility if available
            return new WebDriverManager().createDriver("chrome", false);
        } catch (Exception e) {
            testLogger.error("Failed to create fallback driver", e);
            throw new RuntimeException("Cannot create WebDriver instance", e);
        }
    }
}
