package org.example.controller;

import org.example.model.TestCase;
import org.example.repository.TestCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.Data;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/testcases")
@CrossOrigin(origins = "*")
public class TestCaseController {

    @Autowired
    private TestCaseRepository testCaseRepository;

    @GetMapping
    public ResponseEntity<List<TestCase>> getAllTestCases() {
        List<TestCase> testCases = testCaseRepository.findByIsActiveTrue();
        return ResponseEntity.ok(testCases);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestCase> getTestCase(@PathVariable Long id) {
        Optional<TestCase> testCase = testCaseRepository.findById(id);
        return testCase.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TestCase> createTestCase(@RequestBody TestCaseRequest request) {
        TestCase testCase = new TestCase();
        testCase.setName(request.getName());
        testCase.setDescription(request.getDescription());
        testCase.setTestType(request.getTestType());
        testCase.setTestData(request.getTestData());
        testCase.setExpectedResult(request.getExpectedResult());
        testCase.setPriority(request.getPriority());
        testCase.setTestSuite(request.getTestSuite());
        testCase.setEnvironment(request.getEnvironment());
        testCase.setIsActive(true);

        TestCase savedTestCase = testCaseRepository.save(testCase);
        return ResponseEntity.ok(savedTestCase);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestCase> updateTestCase(@PathVariable Long id, @RequestBody TestCaseRequest request) {
        Optional<TestCase> optionalTestCase = testCaseRepository.findById(id);
        if (optionalTestCase.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TestCase testCase = optionalTestCase.get();
        testCase.setName(request.getName());
        testCase.setDescription(request.getDescription());
        testCase.setTestType(request.getTestType());
        testCase.setTestData(request.getTestData());
        testCase.setExpectedResult(request.getExpectedResult());
        testCase.setPriority(request.getPriority());
        testCase.setTestSuite(request.getTestSuite());
        testCase.setEnvironment(request.getEnvironment());

        TestCase updatedTestCase = testCaseRepository.save(testCase);
        return ResponseEntity.ok(updatedTestCase);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestCase(@PathVariable Long id) {
        Optional<TestCase> testCase = testCaseRepository.findById(id);
        if (testCase.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TestCase tc = testCase.get();
        tc.setIsActive(false);
        testCaseRepository.save(tc);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suite/{testSuite}")
    public ResponseEntity<List<TestCase>> getTestCasesByTestSuite(@PathVariable String testSuite) {
        List<TestCase> testCases = testCaseRepository.findByTestSuiteAndIsActiveTrue(testSuite);
        return ResponseEntity.ok(testCases);
    }

    @GetMapping("/environment/{environment}")
    public ResponseEntity<List<TestCase>> getTestCasesByEnvironment(@PathVariable String environment) {
        List<TestCase> testCases = testCaseRepository.findByEnvironmentAndIsActiveTrue(environment);
        return ResponseEntity.ok(testCases);
    }

    @GetMapping("/meta/testsuites")
    public ResponseEntity<List<String>> getAvailableTestSuites() {
        try {
            List<String> testSuites = testCaseRepository.findByIsActiveTrue()
                .stream()
                .map(TestCase::getTestSuite)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            return ResponseEntity.ok(testSuites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/meta/environments")
    public ResponseEntity<List<String>> getAvailableEnvironments() {
        try {
            List<String> environments = testCaseRepository.findByIsActiveTrue()
                .stream()
                .map(TestCase::getEnvironment)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            return ResponseEntity.ok(environments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Data
    public static class TestCaseRequest {
        private String name;
        private String description;
        private TestCase.TestType testType;
        private String testData;
        private String expectedResult;
        private TestCase.Priority priority;
        private String testSuite;
        private String environment;

        // Explicit getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public TestCase.TestType getTestType() { return testType; }
        public void setTestType(TestCase.TestType testType) { this.testType = testType; }

        public String getTestData() { return testData; }
        public void setTestData(String testData) { this.testData = testData; }

        public String getExpectedResult() { return expectedResult; }
        public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }

        public TestCase.Priority getPriority() { return priority; }
        public void setPriority(TestCase.Priority priority) { this.priority = priority; }

        public String getTestSuite() { return testSuite; }
        public void setTestSuite(String testSuite) { this.testSuite = testSuite; }

        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
    }
}
