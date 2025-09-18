package org.example.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

/**
 * Base class for API tests with common setup and utilities
 */
public class BaseAPITest {
    
    protected Logger testLogger = LoggerFactory.getLogger(this.getClass());
    
    @BeforeMethod
    public void setUp() {
        testLogger.info("Starting API test: " + this.getClass().getSimpleName());
    }
    
    @AfterMethod
    public void tearDown() {
        testLogger.info("Completed API test: " + this.getClass().getSimpleName());
    }
}
