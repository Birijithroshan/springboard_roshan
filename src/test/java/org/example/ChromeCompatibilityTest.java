package org.example;

import org.example.engine.WebUITestExecutor;
import org.example.model.TestCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class to verify Chrome WebDriver initialization without CDP version issues
 */
@SpringBootTest(properties = {"server.port=0"}) // Use random port to avoid conflicts
@ActiveProfiles("test")
public class ChromeCompatibilityTest {

    @Autowired
    private WebUITestExecutor webUITestExecutor;

    @Test
    public void testChromeInitialization() {
        System.out.println("Starting Chrome WebDriver test with Chrome 140.0.7339.185...");

        // Create test data
        Map<String, Object> testData = new HashMap<>();
        testData.put("url", "https://www.google.com");

        // Create test case
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setName("Chrome CDP Compatibility Test");
        testCase.setDescription("Test to verify WebDriver works with Chrome 140 without CDP issues");

        // Execute test
        System.out.println("Initializing WebDriver...");
        webUITestExecutor.execute(testData, testCase);
        System.out.println("WebDriver initialized and executed successfully!");
    }
}
