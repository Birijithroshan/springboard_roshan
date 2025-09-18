package org.example;

import org.example.engine.WebUITestExecutor;
import org.example.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class to verify Chrome WebDriver initialization works without CDP version issues
 */
@Component
@Profile("chrome-test")
public class ChromeDriverTest {

    @Autowired
    private WebUITestExecutor webUITestExecutor;

    public void runTest() {
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
        try {
            webUITestExecutor.execute(testData, testCase);
            System.out.println("WebDriver initialized and executed successfully!");
        } catch (Exception e) {
            System.err.println("WebDriver test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Command line runner bean to execute test when application starts with chrome-test profile
     */
    @Bean
    @Profile("chrome-test")
    public CommandLineRunner chromeTestRunner(ChromeDriverTest chromeDriverTest) {
        return args -> {
            chromeDriverTest.runTest();
        };
    }
}
