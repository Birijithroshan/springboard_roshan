package org.example.controller;

import org.example.model.TestBatch;
import org.example.model.TestExecution;
import org.example.service.TestExecutionService;
import org.example.repository.TestBatchRepository;
import org.example.repository.TestExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.Data;

import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/execution")
@CrossOrigin(origins = "*")
public class TestExecutionController {

    @Autowired
    private TestExecutionService testExecutionService;

    @Autowired
    private TestBatchRepository testBatchRepository;

    @Autowired
    private TestExecutionRepository testExecutionRepository;

    @PostMapping("/batch")
    public ResponseEntity<BatchResponse> executeBatch(@RequestBody BatchRequest request) {
        try {
            // Create and save the batch first to get a persistent ID
            TestBatch batch = new TestBatch();
            batch.setBatchId(java.util.UUID.randomUUID().toString());
            batch.setBatchName("Batch_" + request.getTestSuite() + "_" + java.time.LocalDateTime.now());
            batch.setStatus(TestBatch.BatchStatus.SCHEDULED);
            batch.setEnvironment(request.getEnvironment());
            batch.setParallelThreads(request.getParallelThreads());
            batch = testBatchRepository.save(batch);

            // Asynchronously execute the batch
            testExecutionService.executeBatch(
                    batch,
                    request.getTestSuite(),
                    request.getEnvironment(),
                    request.getParallelThreads()
            );

            // Respond with the actual batch ID
            BatchResponse response = new BatchResponse();
            response.setBatchId(batch.getBatchId());
            response.setStatus("STARTED");
            response.setMessage("Batch execution started successfully with " + request.getParallelThreads() + " parallel threads");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            BatchResponse errorResponse = new BatchResponse();
            errorResponse.setMessage("Failed to start batch execution: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/single/{testCaseId}")
    public ResponseEntity<TestExecution> executeSingleTest(
            @PathVariable Long testCaseId,
            @RequestParam String environment) {
        try {
            TestExecution execution = testExecutionService.executeSingleTest(testCaseId, environment);
            return ResponseEntity.ok(execution);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<TestBatch> getBatchStatus(@PathVariable String batchId) {
        return testBatchRepository.findByBatchId(batchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/batch/{batchId}/executions")
    public ResponseEntity<List<TestExecution>> getBatchExecutions(@PathVariable String batchId) {
        return testBatchRepository.findByBatchId(batchId)
                .map(batch -> ResponseEntity.ok(batch.getTestExecutions()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/batches")
    public ResponseEntity<List<TestBatch>> getAllBatches() {
        List<TestBatch> batches = testBatchRepository.findAllOrderByCreatedAtDesc();
        return ResponseEntity.ok(batches);
    }

    @GetMapping("/batches/recent")
    public ResponseEntity<List<TestBatch>> getRecentBatches(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            List<TestBatch> recentBatches;

            if (limit > 0 && limit <= 100) {
                recentBatches = testBatchRepository.findTopRecentBatches(limit);
            } else {
                recentBatches = testBatchRepository.findRecentBatches(since);
            }

            return ResponseEntity.ok(recentBatches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/batches/status/{status}")
    public ResponseEntity<List<TestBatch>> getBatchesByStatus(@PathVariable String status) {
        try {
            TestBatch.BatchStatus batchStatus = TestBatch.BatchStatus.valueOf(status.toUpperCase());
            List<TestBatch> batches = testBatchRepository.findByStatusOrderByCreatedAtDesc(batchStatus);
            return ResponseEntity.ok(batches);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/batches/active")
    public ResponseEntity<List<TestBatch>> getActiveBatches() {
        List<TestBatch> activeBatches = testBatchRepository.findActiveBatches();
        return ResponseEntity.ok(activeBatches);
    }

    @GetMapping("/executions")
    public ResponseEntity<List<TestExecution>> getAllExecutions() {
        List<TestExecution> executions = testExecutionRepository.findAll();
        return ResponseEntity.ok(executions);
    }

    // New endpoints for parallel execution monitoring
    @GetMapping("/batch/{batchId}/progress")
    public ResponseEntity<BatchProgressResponse> getBatchProgress(@PathVariable String batchId) {
        return testBatchRepository.findByBatchId(batchId)
                .map(batch -> {
                    BatchProgressResponse response = new BatchProgressResponse();
                    response.setBatchId(batch.getBatchId());
                    response.setStatus(batch.getStatus().toString());
                    response.setTotalTests(batch.getTotalTests());
                    response.setPassedTests(batch.getPassedTests());
                    response.setFailedTests(batch.getFailedTests());
                    response.setSkippedTests(batch.getSkippedTests());
                    response.setParallelThreads(batch.getParallelThreads());

                    int completedTests = batch.getPassedTests() + batch.getFailedTests() + batch.getSkippedTests();
                    response.setCompletedTests(completedTests);

                    if (batch.getTotalTests() > 0) {
                        response.setProgressPercentage((completedTests * 100.0) / batch.getTotalTests());
                    }

                    if (batch.getStartTime() != null) {
                        response.setStartTime(batch.getStartTime());
                        if (batch.getEndTime() != null) {
                            response.setEndTime(batch.getEndTime());
                            response.setDuration(java.time.Duration.between(batch.getStartTime(), batch.getEndTime()).toMillis());
                        } else {
                            response.setDuration(java.time.Duration.between(batch.getStartTime(), LocalDateTime.now()).toMillis());
                        }
                    }

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/batch/{batchId}/cancel")
    public ResponseEntity<BatchResponse> cancelBatch(@PathVariable String batchId) {
        try {
            boolean cancelled = testExecutionService.cancelBatch(batchId);
            BatchResponse response = new BatchResponse();
            response.setBatchId(batchId);

            if (cancelled) {
                response.setStatus("CANCELLED");
                response.setMessage("Batch execution cancelled successfully");
                return ResponseEntity.ok(response);
            } else {
                response.setStatus("ERROR");
                response.setMessage("Failed to cancel batch - batch may have already completed");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            BatchResponse errorResponse = new BatchResponse();
            errorResponse.setBatchId(batchId);
            errorResponse.setStatus("ERROR");
            errorResponse.setMessage("Error cancelling batch: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/parallel/status")
    public ResponseEntity<ParallelExecutionStatus> getParallelExecutionStatus() {
        ParallelExecutionStatus status = testExecutionService.getParallelExecutionStatus();
        return ResponseEntity.ok(status);
    }

    @Data
    public static class BatchRequest {
        private String testSuite;
        private String environment;
        private int parallelThreads = 1;

        public String getTestSuite() { return testSuite; }
        public void setTestSuite(String testSuite) { this.testSuite = testSuite; }

        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }

        public int getParallelThreads() { return parallelThreads; }
        public void setParallelThreads(int parallelThreads) { this.parallelThreads = parallelThreads; }
    }

    @Data
    public static class BatchResponse {
        private String batchId;
        private String status;
        private String message;

        public String getBatchId() { return batchId; }
        public void setBatchId(String batchId) { this.batchId = batchId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @Data
    public static class BatchProgressResponse {
        private String batchId;
        private String status;
        private int totalTests;
        private int completedTests;
        private int passedTests;
        private int failedTests;
        private int skippedTests;
        private int parallelThreads;
        private double progressPercentage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long duration;

        // Getters and setters
        public String getBatchId() { return batchId; }
        public void setBatchId(String batchId) { this.batchId = batchId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getTotalTests() { return totalTests; }
        public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

        public int getCompletedTests() { return completedTests; }
        public void setCompletedTests(int completedTests) { this.completedTests = completedTests; }

        public int getPassedTests() { return passedTests; }
        public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

        public int getFailedTests() { return failedTests; }
        public void setFailedTests(int failedTests) { this.failedTests = failedTests; }

        public int getSkippedTests() { return skippedTests; }
        public void setSkippedTests(int skippedTests) { this.skippedTests = skippedTests; }

        public int getParallelThreads() { return parallelThreads; }
        public void setParallelThreads(int parallelThreads) { this.parallelThreads = parallelThreads; }

        public double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
    }

    @Data
    public static class ParallelExecutionStatus {
        private int activeThreads;
        private int maxThreads;
        private int queuedTasks;
        private List<String> activeBatches;
        private long totalExecutedTests;

        // Getters and setters
        public int getActiveThreads() { return activeThreads; }
        public void setActiveThreads(int activeThreads) { this.activeThreads = activeThreads; }

        public int getMaxThreads() { return maxThreads; }
        public void setMaxThreads(int maxThreads) { this.maxThreads = maxThreads; }

        public int getQueuedTasks() { return queuedTasks; }
        public void setQueuedTasks(int queuedTasks) { this.queuedTasks = queuedTasks; }

        public List<String> getActiveBatches() { return activeBatches; }
        public void setActiveBatches(List<String> activeBatches) { this.activeBatches = activeBatches; }

        public long getTotalExecutedTests() { return totalExecutedTests; }
        public void setTotalExecutedTests(long totalExecutedTests) { this.totalExecutedTests = totalExecutedTests; }
    }
}
