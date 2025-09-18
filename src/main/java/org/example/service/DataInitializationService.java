package org.example.service;

import org.example.model.TestCase;
import org.example.model.TestSchedule;
import org.example.repository.TestCaseRepository;
import org.example.repository.TestScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private TestScheduleRepository testScheduleRepository;



    @Override
    public void run(String... args) throws Exception {
        if (testCaseRepository.count() == 0) {
            initializeSampleData();
        }
    }

    private void initializeSampleData() {
        try {
            List<TestCase> sampleTestCases = createSampleTestCases();
            testCaseRepository.saveAll(sampleTestCases);

            List<TestSchedule> sampleSchedules = createSampleSchedules();
            testScheduleRepository.saveAll(sampleSchedules);

            System.out.println("✅ Sample data initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error initializing sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<TestCase> createSampleTestCases() {
        List<TestCase> testCases = new ArrayList<>();

        // Sample Web UI Test Cases
        testCases.add(createTestCase(
            "User Login Functionality",
            "Verify user can login with valid credentials",
            TestCase.TestType.WEB_UI,
            TestCase.Priority.HIGH,
            "Authentication",
            "dev",
            "{\"url\":\"https://demo.testfire.net/login.jsp\",\"username\":\"admin\",\"password\":\"admin123\",\"expectedTitle\":\"Dashboard\"}",
            "User should be successfully logged in and redirected to dashboard"
        ));

        testCases.add(createTestCase(
            "Product Search Feature",
            "Verify product search returns relevant results",
            TestCase.TestType.WEB_UI,
            TestCase.Priority.MEDIUM,
            "E-Commerce",
            "dev",
            "{\"url\":\"https://demo.opencart.com\",\"searchTerm\":\"iPhone\",\"expectedResults\":\">=1\"}",
            "Search should return at least 1 iPhone product in results"
        ));

        testCases.add(createTestCase(
            "Shopping Cart Functionality",
            "Verify items can be added to cart successfully",
            TestCase.TestType.WEB_UI,
            TestCase.Priority.HIGH,
            "E-Commerce",
            "staging",
            "{\"url\":\"https://demo.opencart.com\",\"product\":\"MacBook\",\"quantity\":2}",
            "Product should be added to cart with correct quantity"
        ));

        testCases.add(createTestCase(
            "Form Validation Test",
            "Verify form validation works for required fields",
            TestCase.TestType.WEB_UI,
            TestCase.Priority.MEDIUM,
            "Validation",
            "dev",
            "{\"url\":\"https://www.seleniumeasy.com/test/basic-first-form-demo.html\",\"requiredFields\":[\"user-message\"]}",
            "Form should show validation errors for empty required fields"
        ));

        // Sample API Test Cases
        testCases.add(createTestCase(
            "GET Users API",
            "Verify GET request returns user list",
            TestCase.TestType.API,
            TestCase.Priority.HIGH,
            "UserAPI",
            "dev",
            "{\"method\":\"GET\",\"endpoint\":\"https://reqres.in/api/users?page=2\",\"expectedStatusCode\":200,\"expectedFields\":[\"data\",\"page\",\"per_page\"]}",
            "API should return 200 status with user data array"
        ));

        testCases.add(createTestCase(
            "POST Create User API",
            "Verify POST request creates new user",
            TestCase.TestType.API,
            TestCase.Priority.HIGH,
            "UserAPI",
            "dev",
            "{\"method\":\"POST\",\"endpoint\":\"https://reqres.in/api/users\",\"requestBody\":{\"name\":\"John Doe\",\"job\":\"QA Engineer\"},\"expectedStatusCode\":201,\"expectedFields\":[\"name\",\"job\",\"id\",\"createdAt\"]}",
            "API should return 201 status with created user data"
        ));

        testCases.add(createTestCase(
            "PUT Update User API",
            "Verify PUT request updates user information",
            TestCase.TestType.API,
            TestCase.Priority.MEDIUM,
            "UserAPI",
            "staging",
            "{\"method\":\"PUT\",\"endpoint\":\"https://reqres.in/api/users/2\",\"requestBody\":{\"name\":\"Jane Smith\",\"job\":\"Senior QA\"},\"expectedStatusCode\":200,\"expectedFields\":[\"name\",\"job\",\"updatedAt\"]}",
            "API should return 200 status with updated user data"
        ));

        testCases.add(createTestCase(
            "DELETE User API",
            "Verify DELETE request removes user",
            TestCase.TestType.API,
            TestCase.Priority.MEDIUM,
            "UserAPI",
            "staging",
            "{\"method\":\"DELETE\",\"endpoint\":\"https://reqres.in/api/users/2\",\"expectedStatusCode\":204}",
            "API should return 204 status indicating successful deletion"
        ));

        testCases.add(createTestCase(
            "Authentication API",
            "Verify login API with valid credentials",
            TestCase.TestType.API,
            TestCase.Priority.HIGH,
            "AuthAPI",
            "production",
            "{\"method\":\"POST\",\"endpoint\":\"https://reqres.in/api/login\",\"requestBody\":{\"email\":\"eve.holt@reqres.in\",\"password\":\"cityslicka\"},\"expectedStatusCode\":200,\"expectedFields\":[\"token\"]}",
            "API should return 200 status with authentication token"
        ));

        testCases.add(createTestCase(
            "File Upload Test",
            "Verify file upload functionality works",
            TestCase.TestType.WEB_UI,
            TestCase.Priority.MEDIUM,
            "FileOperations",
            "dev",
            "{\"url\":\"https://www.seleniumeasy.com/test/upload-file-demo.html\",\"filePath\":\"sample.txt\",\"fileType\":\"text/plain\"}",
            "File should be uploaded successfully with confirmation message"
        ));

        return testCases;
    }

    private TestCase createTestCase(String name, String description, TestCase.TestType testType,
                                  TestCase.Priority priority, String testSuite, String environment,
                                  String testData, String expectedResult) {
        TestCase testCase = new TestCase();
        testCase.setName(name);
        testCase.setDescription(description);
        testCase.setTestType(testType);
        testCase.setPriority(priority);
        testCase.setTestSuite(testSuite);
        testCase.setEnvironment(environment);
        testCase.setTestData(testData);
        testCase.setExpectedResult(expectedResult);
        testCase.setIsActive(true);
        testCase.setCreatedBy("System");
        testCase.setCategory("Demo");
        return testCase;
    }

    private List<TestSchedule> createSampleSchedules() {
        List<TestSchedule> schedules = new ArrayList<>();

        schedules.add(createSchedule(
            "Daily Smoke Tests",
            "0 0 6 * * ?", // Every day at 6 AM
            "Authentication",
            "dev",
            2,
            true
        ));

        schedules.add(createSchedule(
            "Weekly Regression Tests",
            "0 0 2 ? * SUN", // Every Sunday at 2 AM
            "E-Commerce",
            "staging",
            3,
            true
        ));

        schedules.add(createSchedule(
            "API Health Check",
            "0 */30 * * * ?", // Every 30 minutes
            "UserAPI",
            "production",
            1,
            true
        ));

        return schedules;
    }

    private TestSchedule createSchedule(String name, String cronExpression, String testSuite,
                                      String environment, int parallelThreads, boolean isActive) {
        TestSchedule schedule = new TestSchedule();
        schedule.setScheduleName(name);
        schedule.setCronExpression(cronExpression);
        schedule.setTestSuite(testSuite);
        schedule.setEnvironment(environment);
        schedule.setParallelThreads(parallelThreads);
        schedule.setIsActive(isActive);
        schedule.setNotificationEnabled(false);
        return schedule;
    }
}
