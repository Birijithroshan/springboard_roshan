package org.example.controller;

import org.example.model.TestCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class DemoDataController {

    @GetMapping("/test-cases")
    public ResponseEntity<List<TestCase>> getDemoTestCases() {
        List<TestCase> demoData = new ArrayList<>();

        // Create sample test cases with realistic data
        demoData.add(createSampleTestCase(1L, "User Login Test", "WEB_UI", "HIGH", "Authentication", "dev"));
        demoData.add(createSampleTestCase(2L, "API User Registration", "API", "MEDIUM", "UserAPI", "staging"));
        demoData.add(createSampleTestCase(3L, "Product Search", "WEB_UI", "MEDIUM", "E-Commerce", "dev"));
        demoData.add(createSampleTestCase(4L, "Payment Processing", "WEB_UI", "HIGH", "Payment", "production"));
        demoData.add(createSampleTestCase(5L, "Data Validation API", "API", "LOW", "ValidationAPI", "dev"));
        demoData.add(createSampleTestCase(6L, "File Upload Test", "WEB_UI", "MEDIUM", "FileOperations", "staging"));
        demoData.add(createSampleTestCase(7L, "User Logout Test", "WEB_UI", "LOW", "Authentication", "dev"));
        demoData.add(createSampleTestCase(8L, "GET Users API", "API", "HIGH", "UserAPI", "production"));

        return ResponseEntity.ok(demoData);
    }

    @GetMapping("/executions")
    public ResponseEntity<List<Map<String, Object>>> getDemoExecutions() {
        List<Map<String, Object>> executions = new ArrayList<>();
        
        // Create sample execution data
        executions.add(createSampleExecution(1L, "User Login Test", "PASSED", "dev", 
            LocalDateTime.now().minusHours(2), "45.2"));
        executions.add(createSampleExecution(2L, "API User Registration", "FAILED", "staging", 
            LocalDateTime.now().minusHours(1), "12.8"));
        executions.add(createSampleExecution(3L, "Product Search", "PASSED", "dev", 
            LocalDateTime.now().minusMinutes(30), "32.1"));
        executions.add(createSampleExecution(4L, "Payment Processing", "RUNNING", "production", 
            LocalDateTime.now().minusMinutes(5), "0"));

        return ResponseEntity.ok(executions);
    }

    @GetMapping("/batches")
    public ResponseEntity<List<Map<String, Object>>> getDemoBatches() {
        List<Map<String, Object>> batches = new ArrayList<>();

        // Create sample batch data
        batches.add(createSampleBatch("batch-001", "COMPLETED", "dev", "Authentication", 
            LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(2).minusMinutes(45)));
        batches.add(createSampleBatch("batch-002", "COMPLETED", "staging", "E-Commerce", 
            LocalDateTime.now().minusHours(6), LocalDateTime.now().minusHours(5).minusMinutes(30)));
        batches.add(createSampleBatch("batch-003", "RUNNING", "production", "UserAPI", 
            LocalDateTime.now().minusMinutes(15), null));
        batches.add(createSampleBatch("batch-004", "FAILED", "dev", "Payment", 
            LocalDateTime.now().minusHours(12), LocalDateTime.now().minusHours(11).minusMinutes(45)));

        return ResponseEntity.ok(batches);
    }

    @GetMapping("/sample-data")
    public ResponseEntity<Map<String, Object>> getAllSampleData() {
        Map<String, Object> sampleData = new HashMap<>();
        
        sampleData.put("testCases", getDemoTestCases().getBody());
        sampleData.put("executions", getDemoExecutions().getBody());
        sampleData.put("batches", getDemoBatches().getBody());
        
        // Add sample metrics
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalTestCases", 25);
        metrics.put("totalExecutions", 142);
        metrics.put("activeSchedules", 4);
        metrics.put("successRate", 89.7);
        sampleData.put("metrics", metrics);

        // Add sample environments
        Map<String, Object> environments = new HashMap<>();
        environments.put("dev", Map.of("status", "Healthy", "tests", 15, "lastRun", "2 hours ago"));
        environments.put("staging", Map.of("status", "Warning", "tests", 8, "lastRun", "30 minutes ago"));
        environments.put("production", Map.of("status", "Healthy", "tests", 2, "lastRun", "6 hours ago"));
        sampleData.put("environments", environments);

        return ResponseEntity.ok(sampleData);
    }

    @PostMapping("/create-sample-test")
    public ResponseEntity<Map<String, Object>> createSampleTest(@RequestBody Map<String, Object> testData) {
        // Simulate test creation
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("testId", System.currentTimeMillis());
        response.put("message", "Sample test case created successfully!");
        response.put("testName", testData.get("name"));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/execute-sample-batch")
    public ResponseEntity<Map<String, Object>> executeSampleBatch(@RequestBody Map<String, Object> batchData) {
        // Simulate batch execution
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("batchId", "demo-batch-" + System.currentTimeMillis());
        response.put("status", "STARTED");
        response.put("message", "Sample batch execution started!");
        response.put("testSuite", batchData.get("testSuite"));
        response.put("environment", batchData.get("environment"));
        
        return ResponseEntity.ok(response);
    }

    private TestCase createSampleTestCase(Long id, String name, String testType, String priority, 
                                        String testSuite, String environment) {
        TestCase testCase = new TestCase();
        testCase.setId(id);
        testCase.setName(name);
        testCase.setDescription("Sample test case: " + name);
        testCase.setTestType(TestCase.TestType.valueOf(testType));
        testCase.setPriority(TestCase.Priority.valueOf(priority));
        testCase.setTestSuite(testSuite);
        testCase.setEnvironment(environment);
        testCase.setIsActive(true);
        testCase.setCreatedBy("Demo System");
        
        // Set sample test data based on type
        if ("API".equals(testType)) {
            testCase.setTestData("{\"method\":\"GET\",\"endpoint\":\"https://api.example.com/users\",\"expectedStatusCode\":200}");
            testCase.setExpectedResult("API should return 200 status with user data");
        } else {
            testCase.setTestData("{\"url\":\"https://demo.example.com\",\"username\":\"testuser\",\"password\":\"password123\"}");
            testCase.setExpectedResult("User should be able to perform the required action successfully");
        }
        
        return testCase;
    }

    private Map<String, Object> createSampleExecution(Long id, String testName, String status, 
                                                     String environment, LocalDateTime startTime, String duration) {
        Map<String, Object> execution = new HashMap<>();
        execution.put("id", id);
        execution.put("testName", testName);
        execution.put("status", status);
        execution.put("environment", environment);
        execution.put("startTime", startTime.toString());
        execution.put("duration", duration + "s");
        execution.put("batchId", "batch-" + (id % 3 + 1));
        
        return execution;
    }

    private Map<String, Object> createSampleBatch(String batchId, String status, String environment, 
                                                 String testSuite, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> batch = new HashMap<>();
        batch.put("batchId", batchId);
        batch.put("status", status);
        batch.put("environment", environment);
        batch.put("testSuite", testSuite);
        batch.put("createdAt", startTime.toString());
        if (endTime != null) {
            batch.put("completedAt", endTime.toString());
        }
        batch.put("parallelThreads", 2);
        
        return batch;
    }
}
