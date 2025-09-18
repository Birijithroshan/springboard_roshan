package org.example.service;

import org.example.dto.TestExecutionResultDTO;
import org.example.model.TestBatch;
import org.example.model.TestCase;
import org.example.model.TestExecution;
import org.example.model.TestSchedule;
import org.example.repository.TestBatchRepository;
import org.example.repository.TestCaseRepository;
import org.example.repository.TestExecutionRepository;
import org.example.engine.WebUITestExecutor;
import org.example.engine.ApiTestExecutor;
import org.example.engine.TestExecutionEngine;
import org.example.controller.TestExecutionController.ParallelExecutionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class TestExecutionService {
    private static final Logger log = LoggerFactory.getLogger(TestExecutionService.class);

    private final TestCaseRepository testCaseRepository;
    private final TestExecutionRepository testExecutionRepository;
    private final TestBatchRepository testBatchRepository;
    private final WebUITestExecutor webUITestExecutor;
    private final ApiTestExecutor apiTestExecutor;

    // Enhanced parallel execution management
    private final Map<String, ExecutorService> batchExecutors = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> batchFutures = new ConcurrentHashMap<>();
    private final Map<String, Boolean> cancellationFlags = new ConcurrentHashMap<>();

    @Autowired
    public TestExecutionService(
            TestCaseRepository testCaseRepository,
            TestExecutionRepository testExecutionRepository,
            TestBatchRepository testBatchRepository,
            WebUITestExecutor webUITestExecutor,
            ApiTestExecutor apiTestExecutor) {
        this.testCaseRepository = testCaseRepository;
        this.testExecutionRepository = testExecutionRepository;
        this.testBatchRepository = testBatchRepository;
        this.webUITestExecutor = webUITestExecutor;
        this.apiTestExecutor = apiTestExecutor;
    }

    @Async("testExecutionExecutor")
    public CompletableFuture<TestBatch> executeBatch(TestBatch batch, String testSuite, String environment, int parallelThreads) {
        String batchId = batch.getBatchId();
        log.info("Starting enhanced parallel batch execution: {} with {} threads", batchId, parallelThreads);

        // Create dedicated executor for this batch
        ExecutorService batchExecutor = Executors.newFixedThreadPool(parallelThreads);
        batchExecutors.put(batchId, batchExecutor);
        cancellationFlags.put(batchId, false);

        // Update batch status to RUNNING
        batch.setStatus(TestBatch.BatchStatus.RUNNING);
        batch.setStartTime(LocalDateTime.now());

        try {
            // Get test cases for execution
            log.info("Searching for test cases with testSuite='{}' and environment='{}'", testSuite, environment);
            List<TestCase> testCases = testCaseRepository.findByTestSuiteAndEnvironment(testSuite, environment);

            if (testCases == null || testCases.isEmpty()) {
                log.warn("No test cases found for testSuite='{}' and environment='{}'. Available test suites: {}",
                    testSuite, environment, getAvailableTestSuites());
                batch.setStatus(TestBatch.BatchStatus.FAILED);
                batch.setEndTime(LocalDateTime.now());
                batch.setTotalTests(0);
                testBatchRepository.save(batch);
                return CompletableFuture.completedFuture(batch);
            }

            log.info("Found {} test cases for execution in batch: {}", testCases.size(), batchId);
            batch.setTotalTests(testCases.size());

            // Save batch initially
            batch = testBatchRepository.save(batch);

            // Execute tests in parallel with enhanced monitoring
            executeTestsInParallelEnhanced(testCases, batch, environment, batchExecutor);

            // Check if batch was cancelled
            if (cancellationFlags.get(batchId)) {
                batch.setStatus(TestBatch.BatchStatus.CANCELLED);
                log.info("Batch execution cancelled: {}", batchId);
            } else {
                batch.setStatus(TestBatch.BatchStatus.COMPLETED);
                log.info("Batch execution completed: {}", batchId);
            }

            batch.setEndTime(LocalDateTime.now());

        } catch (Exception e) {
            log.error("Batch execution failed: {}", batchId, e);
            batch.setStatus(TestBatch.BatchStatus.FAILED);
            batch.setEndTime(LocalDateTime.now());
        } finally {
            // Cleanup resources
            cleanupBatchExecution(batchId);
            batch = testBatchRepository.save(batch);
        }

        return CompletableFuture.completedFuture(batch);
    }

    private List<String> getAvailableTestSuites() {
        try {
            return testCaseRepository.findByIsActiveTrue()
                .stream()
                .map(TestCase::getTestSuite)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting available test suites", e);
            return List.of("Error retrieving test suites");
        }
    }

    private void executeTestsInParallelEnhanced(List<TestCase> testCases, TestBatch batch, String environment, ExecutorService executor) {
        String batchId = batch.getBatchId();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        try {
            for (TestCase testCase : testCases) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    // Check cancellation flag before executing
                    if (cancellationFlags.get(batchId)) {
                        log.debug("Skipping test execution due to cancellation: {}", testCase.getName());
                        return;
                    }

                    try {
                        log.debug("Executing test case: {} in batch: {}", testCase.getName(), batchId);
                        TestExecution execution = executeTestCase(testCase, environment);
                        execution.setTestBatch(batch);

                        // Update batch stats synchronously
                        updateBatchStats(batch, execution);
                        testExecutionRepository.save(execution);

                        log.debug("Completed test case: {} with status: {}", testCase.getName(), execution.getStatus());
                    } catch (Exception e) {
                        log.error("Test execution failed for test case: {} in batch: {}", testCase.getName(), batchId, e);

                        // Create failed execution record
                        TestExecution failedExecution = new TestExecution();
                        failedExecution.setTestCase(testCase);
                        failedExecution.setTestBatch(batch);
                        failedExecution.setStatus(TestExecution.ExecutionStatus.ERROR);
                        failedExecution.setStartTime(LocalDateTime.now());
                        failedExecution.setEndTime(LocalDateTime.now());
                        failedExecution.setErrorMessage("Execution failed: " + e.getMessage());

                        updateBatchStats(batch, failedExecution);
                        testExecutionRepository.save(failedExecution);
                    }
                }, executor);

                futures.add(future);
                batchFutures.put(batchId + "_" + testCase.getId(), future);
            }

            // Store and wait for all tasks to complete
            CompletableFuture<Void> batchFuture = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            batchFutures.put(batchId, batchFuture);

            // Wait for completion or timeout
            try {
                batchFuture.get(60, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("Error waiting for test execution completion", e);
                cancellationFlags.put(batchId, true);
            }

            // Shutdown executor
            executor.shutdown();
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                log.warn("Batch execution timed out, forcing shutdown: {}", batchId);
                executor.shutdownNow();
                cancellationFlags.put(batchId, true);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            cancellationFlags.put(batchId, true);
            log.error("Batch execution interrupted: {}", batchId, e);
        }
    }

    private TestExecution executeTestCase(TestCase testCase, String environment) {
        TestExecution execution;

        switch (testCase.getTestType()) {
            case WEB_UI:
                // Create test data for WebUI execution
                Map<String, Object> webUIData = new HashMap<>();
                webUIData.put("browser", "chrome");
                webUIData.put("environment", environment);
                TestExecutionEngine.TestExecutionResult webResult = webUITestExecutor.execute(webUIData, testCase);
                execution = createTestExecution(testCase, webResult);
                break;
            case API:
                // Create test data for API execution
                // Map<String, Object> apiData = new HashMap<>();
                // apiData.put("environment", environment);
                execution = apiTestExecutor.executeApiTest(testCase, environment);
                break;
            default:
                throw new UnsupportedOperationException("Test type not supported: " + testCase.getTestType());
        }

        return execution;
    }

    private synchronized void updateBatchStats(TestBatch batch, TestExecution execution) {
        switch (execution.getStatus()) {
            case PASSED:
                batch.setPassedTests(batch.getPassedTests() + 1);
                break;
            case FAILED:
            case ERROR:
                batch.setFailedTests(batch.getFailedTests() + 1);
                break;
            case SKIPPED:
                batch.setSkippedTests(batch.getSkippedTests() + 1);
                break;
            case PENDING:
            case RUNNING:
                // Don't update stats for pending/running tests
                break;
        }

        // Update batch in database periodically
        testBatchRepository.save(batch);
    }

    // New method for batch cancellation
    public boolean cancelBatch(String batchId) {
        log.info("Attempting to cancel batch: {}", batchId);

        try {
            // Set cancellation flag
            cancellationFlags.put(batchId, true);

            // Get the executor for this batch
            ExecutorService executor = batchExecutors.get(batchId);
            if (executor != null && !executor.isShutdown()) {
                executor.shutdownNow();
                log.info("Executor shutdown initiated for batch: {}", batchId);
            }

            // Cancel individual futures
            batchFutures.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(batchId))
                .forEach(entry -> {
                    entry.getValue().cancel(true);
                    log.debug("Cancelled future: {}", entry.getKey());
                });

            // Update batch status in database
            testBatchRepository.findByBatchId(batchId).ifPresent(batch -> {
                if (batch.getStatus() == TestBatch.BatchStatus.RUNNING ||
                    batch.getStatus() == TestBatch.BatchStatus.SCHEDULED) {
                    batch.setStatus(TestBatch.BatchStatus.CANCELLED);
                    batch.setEndTime(LocalDateTime.now());
                    testBatchRepository.save(batch);
                    log.info("Batch status updated to CANCELLED: {}", batchId);
                }
            });

            return true;

        } catch (Exception e) {
            log.error("Error cancelling batch: {}", batchId, e);
            return false;
        }
    }

    // New method to get parallel execution status
    public ParallelExecutionStatus getParallelExecutionStatus() {
        ParallelExecutionStatus status = new ParallelExecutionStatus();

        // Count active executors and threads
        int activeThreads = 0;
        int maxThreads = 0;
        int queuedTasks = 0;

        for (ExecutorService executor : batchExecutors.values()) {
            if (executor instanceof ThreadPoolExecutor threadPool) {
                activeThreads += threadPool.getActiveCount();
                maxThreads += threadPool.getMaximumPoolSize();
                queuedTasks += threadPool.getQueue().size();
            }
        }

        status.setActiveThreads(activeThreads);
        status.setMaxThreads(maxThreads);
        status.setQueuedTasks(queuedTasks);

        // Get active batches
        List<String> activeBatches = testBatchRepository.findActiveBatches()
            .stream()
            .map(TestBatch::getBatchId)
            .collect(Collectors.toList());
        status.setActiveBatches(activeBatches);

        // Get total executed tests count
        long totalExecutedTests = testExecutionRepository.count();
        status.setTotalExecutedTests(totalExecutedTests);

        return status;
    }

    private void cleanupBatchExecution(String batchId) {
        log.debug("Cleaning up resources for batch: {}", batchId);

        // Remove and shutdown executor with proper resource management
        ExecutorService executor = batchExecutors.remove(batchId);
        if (executor != null) {
            try {
                executor.shutdown();
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Remove futures
        batchFutures.entrySet().removeIf(entry -> entry.getKey().startsWith(batchId));

        // Remove cancellation flag
        cancellationFlags.remove(batchId);

        log.debug("Cleanup completed for batch: {}", batchId);
    }

    public TestExecution executeSingleTest(Long testCaseId, String environment) {
        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("Test case not found: " + testCaseId));

        TestExecution execution = executeTestCase(testCase, environment);
        return testExecutionRepository.save(execution);
    }

    @Async("testExecutionExecutor")
    public CompletableFuture<TestExecutionResultDTO> executeScheduledTests(TestSchedule schedule) {
        try {
            log.info("Executing scheduled tests for: {}", schedule.getName());

            // Create a new batch for scheduled execution
            TestBatch batch = new TestBatch();
            batch.setBatchId("SCHEDULED_" + schedule.getId() + "_" + System.currentTimeMillis());
            batch.setName("Scheduled: " + schedule.getName());
            batch.setEnvironment(schedule.getEnvironment());
            batch.setStatus(TestBatch.BatchStatus.RUNNING);
            batch.setStartTime(LocalDateTime.now());

            batch = testBatchRepository.save(batch);

            // Execute the batch
            CompletableFuture<TestBatch> batchFuture = executeBatch(
                batch,
                "scheduled",
                schedule.getEnvironment(),
                schedule.getParallelThreads()
            );

            // Convert to DTO
            return batchFuture.thenApply(this::convertToResultDTO);

        } catch (Exception e) {
            log.error("Error executing scheduled tests", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public int cleanupOldExecutions(LocalDateTime cutoffDate) {
        try {
            log.info("Cleaning up test executions older than: {}", cutoffDate);

            List<TestExecution> oldExecutions = testExecutionRepository.findByStartTimeBefore(cutoffDate);
            int deletedCount = oldExecutions.size();

            testExecutionRepository.deleteAll(oldExecutions);

            log.info("Cleaned up {} old test executions", deletedCount);
            return deletedCount;

        } catch (Exception e) {
            log.error("Error during cleanup", e);
            return 0;
        }
    }

    public void generateWeeklySummaryReport(LocalDateTime weekStart, LocalDateTime weekEnd) {
        try {
            log.info("Generating weekly summary report from {} to {}", weekStart, weekEnd);

            List<TestBatch> weeklyBatches = testBatchRepository.findByStartTimeBetween(weekStart, weekEnd);

            // Generate summary statistics
            int totalBatches = weeklyBatches.size();
            int totalTests = weeklyBatches.stream().mapToInt(TestBatch::getTotalTests).sum();
            int totalPassed = weeklyBatches.stream().mapToInt(TestBatch::getPassedTests).sum();
            int totalFailed = weeklyBatches.stream().mapToInt(TestBatch::getFailedTests).sum();

            log.info("Weekly Summary - Batches: {}, Tests: {}, Passed: {}, Failed: {}",
                totalBatches, totalTests, totalPassed, totalFailed);

            // In a real implementation, you would generate and store the summary report

        } catch (Exception e) {
            log.error("Error generating weekly summary report", e);
        }
    }

    private TestExecutionResultDTO convertToResultDTO(TestBatch batch) {
        TestExecutionResultDTO dto = new TestExecutionResultDTO();
        dto.setBatchId(batch.getBatchId());
        dto.setStatus(batch.getStatus().toString());
        dto.setStartTime(batch.getStartTime());
        dto.setEndTime(batch.getEndTime());
        dto.setTotalTests(batch.getTotalTests());
        dto.setPassedTests(batch.getPassedTests());
        dto.setFailedTests(batch.getFailedTests());
        dto.setSkippedTests(batch.getSkippedTests());
        dto.setEnvironment(batch.getEnvironment());

        if (batch.getStartTime() != null && batch.getEndTime() != null) {
            dto.setDuration(java.time.Duration.between(batch.getStartTime(), batch.getEndTime()).toMillis());
        }

        return dto;
    }

    private TestExecution createTestExecution(TestCase testCase, TestExecutionEngine.TestExecutionResult result) {
        TestExecution execution = new TestExecution();
        execution.setTestCase(testCase);
        execution.setExecutionId(java.util.UUID.randomUUID().toString()); // Set required executionId
        execution.setStatus(result.isSuccess() ? TestExecution.ExecutionStatus.PASSED : TestExecution.ExecutionStatus.FAILED);
        execution.setStartTime(LocalDateTime.now().minusSeconds(1)); // Simulate start time
        execution.setEndTime(LocalDateTime.now());
        execution.setExecutionDuration(1000L); // Default duration
        execution.setExecutionLogs(result.getExecutionLogs());
        execution.setErrorMessage(result.getErrorMessage());
        execution.setEnvironment(testCase.getEnvironment()); // Set environment from test case

        if (result.getScreenshotPaths() != null && !result.getScreenshotPaths().isEmpty()) {
            execution.setScreenshotPaths(String.join(",", result.getScreenshotPaths()));
        }
        
        return execution;
    }
}
