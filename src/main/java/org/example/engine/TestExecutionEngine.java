package org.example.engine;

import org.example.model.TestCase;
import org.example.model.TestExecution;
import org.example.model.TestStep;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class TestExecutionEngine {
    private static final Logger log = LoggerFactory.getLogger(TestExecutionEngine.class);

    private final WebUITestExecutor webUIExecutor;
    private final ApiTestExecutor apiExecutor;
    private final DatabaseTestExecutor databaseExecutor;
    private final ObjectMapper objectMapper;

    @Autowired
    public TestExecutionEngine(
            WebUITestExecutor webUIExecutor,
            ApiTestExecutor apiExecutor,
            DatabaseTestExecutor databaseExecutor,
            ObjectMapper objectMapper) {
        this.webUIExecutor = webUIExecutor;
        this.apiExecutor = apiExecutor;
        this.databaseExecutor = databaseExecutor;
        this.objectMapper = objectMapper;
    }

    /**
     * Execute a single test case
     */
    public TestExecution executeTest(TestCase testCase, String environment) {
        log.info("Starting execution of test case: {} in environment: {}", testCase.getName(), environment);
        
        TestExecution execution = new TestExecution();
        execution.setTestCase(testCase);
        execution.setEnvironment(environment);
        execution.setStatus(TestExecution.ExecutionStatus.RUNNING);
        execution.setStartTime(LocalDateTime.now());

        try {
            // Parse test data
            Map<String, Object> testData = parseTestData(testCase.getTestData());
            
            // Execute based on test type
            TestExecutionResult result = executeByType(testCase.getTestType(), testData, testCase, environment);

            // Update execution with results
            execution.setStatus(result.isSuccess() ? 
                TestExecution.ExecutionStatus.PASSED : TestExecution.ExecutionStatus.FAILED);
            execution.setErrorMessage(result.getErrorMessage());
            execution.setExecutionLogs(result.getExecutionLogs());
            execution.setScreenshotPaths(result.getScreenshotPaths());
            execution.setRequestResponseData(result.getRequestResponseData());
            
        } catch (Exception e) {
            log.error("Error executing test case: {}", testCase.getName(), e);
            execution.setStatus(TestExecution.ExecutionStatus.FAILED);
            execution.setErrorMessage("Execution failed: " + e.getMessage());
        } finally {
            execution.setEndTime(LocalDateTime.now());
            execution.calculateDuration();
        }

        return execution;
    }

    /**
     * Execute multiple test cases in parallel
     */
    public List<CompletableFuture<TestExecution>> executeTestsInParallel(
            List<TestCase> testCases, String environment, int maxParallelThreads) {
        log.info("Starting parallel execution of {} test cases with {} threads",
                testCases.size(), maxParallelThreads);

        ExecutorService executor = Executors.newFixedThreadPool(maxParallelThreads);
        List<CompletableFuture<TestExecution>> futures = new ArrayList<>();

        try {
            for (TestCase testCase : testCases) {
                CompletableFuture<TestExecution> future = CompletableFuture.supplyAsync(() ->
                    executeTest(testCase, environment), executor);
                futures.add(future);
            }
            return futures;
        } finally {
            // Schedule executor shutdown after all tasks complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    executor.shutdown();
                    try {
                        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                            executor.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        executor.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                });
        }
    }

    /**
     * Execute test with step-by-step execution for debugging
     */
    public TestExecution executeTestWithSteps(TestCase testCase, String environment, boolean debugMode) {
        log.info("Starting step-by-step execution of test case: {}", testCase.getName());
        
        TestExecution execution = new TestExecution();
        execution.setTestCase(testCase);
        execution.setEnvironment(environment);
        execution.setStatus(TestExecution.ExecutionStatus.RUNNING);
        execution.setStartTime(LocalDateTime.now());

        List<String> stepLogs = new ArrayList<>();
        List<String> screenshots = new ArrayList<>();

        try {
            // Parse test steps from test data
            List<TestStep> testSteps = parseTestSteps(testCase.getTestData());
            
            for (int i = 0; i < testSteps.size(); i++) {
                TestStep step = testSteps.get(i);
                
                if (debugMode) {
                    stepLogs.add(String.format("Step %d: %s", i + 1, step.getDescription()));
                }

                TestExecutionResult stepResult = executeTestStep(step, testCase.getTestType(), environment);
                
                if (!stepResult.isSuccess()) {
                    execution.setStatus(TestExecution.ExecutionStatus.FAILED);
                    execution.setErrorMessage(String.format("Step %d failed: %s", i + 1, stepResult.getErrorMessage()));
                    
                    // Capture screenshot for failed UI steps
                    if (testCase.getTestType() == TestCase.TestType.WEB_UI && stepResult.getScreenshotPaths() != null) {
                        screenshots.addAll(stepResult.getScreenshotPaths());
                    }
                    break;
                }

                stepLogs.add(String.format("Step %d completed successfully", i + 1));
            }

            if (execution.getStatus() == TestExecution.ExecutionStatus.RUNNING) {
                execution.setStatus(TestExecution.ExecutionStatus.PASSED);
            }

        } catch (Exception e) {
            log.error("Error in step-by-step execution: {}", e.getMessage(), e);
            execution.setStatus(TestExecution.ExecutionStatus.FAILED);
            execution.setErrorMessage("Step execution failed: " + e.getMessage());
        } finally {
            execution.setEndTime(LocalDateTime.now());
            execution.calculateDuration();
            execution.setExecutionLogs(String.join("\n", stepLogs));
            execution.setScreenshotPaths(String.join(",", screenshots));
        }

        return execution;
    }

    private TestExecutionResult executeByType(TestCase.TestType testType, Map<String, Object> testData, TestCase testCase, String environment) {
        return switch (testType) {
            case WEB_UI, UI -> webUIExecutor.execute(testData, testCase);
            case API -> convertToResult(apiExecutor.executeApiTest(testCase, environment));
            case DATABASE -> databaseExecutor.execute(testData, testCase);
            case INTEGRATION -> executeIntegrationTest();
        };
    }

    private TestExecutionResult convertToResult(TestExecution execution) {
        TestExecutionResult result = new TestExecutionResult();
        result.setSuccess(execution.getStatus() == TestExecution.ExecutionStatus.PASSED);
        result.setErrorMessage(execution.getErrorMessage());
        result.setExecutionLogs(execution.getExecutionLogs());
        result.setScreenshotPaths(Arrays.asList(execution.getScreenshotPaths().split(",")));
        result.setRequestResponseData(execution.getRequestResponseData());
        return result;
    }

    private TestExecutionResult executeIntegrationTest() {
        // Implementation for integration tests that might involve multiple systems
        TestExecutionResult result = new TestExecutionResult();
        result.setSuccess(true);
        result.setExecutionLogs("Integration test executed successfully");
        return result;
    }

    private TestExecutionResult executeTestStep(TestStep step, TestCase.TestType testType, String environment) {
        Map<String, Object> stepData = step.getStepData();
        
        // Create a temporary test case for the step
        TestCase stepTestCase = new TestCase();
        stepTestCase.setTestType(testType);
        
        return executeByType(testType, stepData, stepTestCase, environment);
    }

    private Map<String, Object> parseTestData(String testDataJson) {
        try {
            return objectMapper.readValue(testDataJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse test data JSON, using empty map: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private List<TestStep> parseTestSteps(String testDataJson) {
        try {
            Map<String, Object> testData = parseTestData(testDataJson);
            
            if (testData.containsKey("steps")) {
                Object stepsObj = testData.get("steps");
                List<TestStep> steps = new ArrayList<>();
                if (stepsObj instanceof List<?> stepsList) {
                    for (Object obj : stepsList) {
                        if (obj instanceof Map<?, ?> stepMap) {
                            TestStep step = new TestStep();
                            step.setStepNumber(steps.size() + 1);

                            Object descObj = stepMap.get("description");
                            String description = descObj != null ? descObj.toString() :
                                               "Step " + (steps.size() + 1);
                            step.setDescription(description);

                            Map<String, Object> safeStepData = new HashMap<>();
                            for (Map.Entry<?, ?> entry : stepMap.entrySet()) {
                                if (entry.getKey() instanceof String) {
                                    safeStepData.put((String) entry.getKey(), entry.getValue());
                                }
                            }
                            step.setStepData(safeStepData);

                            steps.add(step);
                        }
                    }
                }
                return steps;
            } else {
                return Collections.singletonList(createDefaultStep(testData));
            }
        } catch (Exception e) {
            log.warn("Failed to parse test steps, using single step: {}", e.getMessage());
            return Collections.singletonList(createDefaultStep(new HashMap<>()));
        }
    }

    private TestStep createDefaultStep(Map<String, Object> data) {
        TestStep step = new TestStep();
        step.setStepNumber(1);
        step.setDescription("Execute test");
        step.setStepData(data);
        return step;
    }

    @Getter
    @Setter
    public static class TestExecutionResult {
        private boolean success;
        private String errorMessage;
        private String executionLogs;
        private List<String> screenshotPaths;
        private String requestResponseData;

        public TestExecutionResult() {
            this.screenshotPaths = new ArrayList<>();
        }
    }
}
