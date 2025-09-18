package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TestExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "test_case_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TestCase testCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_batch_id")
    @JsonBackReference
    private TestBatch testBatch;

    @Column(name = "execution_id", nullable = false)
    private String executionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "execution_duration")
    private Long executionDuration; // in milliseconds

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "screenshot_path")
    private String screenshotPath;

    @Column(name = "log_file_path")
    private String logFilePath;

    @Column(name = "environment")
    private String environment;

    @Column(name = "browser")
    private String browser;

    @Column(name = "executed_by")
    private String executedBy;

    @Column(name = "actual_result", columnDefinition = "TEXT")
    private String actualResult;

    @Column(name = "execution_logs", columnDefinition = "TEXT")
    private String executionLogs;

    @Column(name = "screenshot_paths", columnDefinition = "TEXT")
    private String screenshotPaths;

    @Column(name = "request_response_data", columnDefinition = "TEXT")
    private String requestResponseData;

    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (executionId == null || executionId.isEmpty()) {
            executionId = java.util.UUID.randomUUID().toString();
        }
    }

    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            executionDuration = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    // Explicit getters and setters to fix Lombok issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TestCase getTestCase() { return testCase; }
    public void setTestCase(TestCase testCase) { this.testCase = testCase; }

    public TestBatch getTestBatch() { return testBatch; }
    public void setTestBatch(TestBatch testBatch) { this.testBatch = testBatch; }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public ExecutionStatus getStatus() { return status; }
    public void setStatus(ExecutionStatus status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getExecutionDuration() { return executionDuration; }
    public void setExecutionDuration(Long executionDuration) { this.executionDuration = executionDuration; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }

    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }

    public String getLogFilePath() { return logFilePath; }
    public void setLogFilePath(String logFilePath) { this.logFilePath = logFilePath; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getExecutedBy() { return executedBy; }
    public void setExecutedBy(String executedBy) { this.executedBy = executedBy; }

    public String getActualResult() { return actualResult; }
    public void setActualResult(String actualResult) { this.actualResult = actualResult; }

    public String getExecutionLogs() { return executionLogs; }
    public void setExecutionLogs(String executionLogs) { this.executionLogs = executionLogs; }

    public String getScreenshotPaths() { return screenshotPaths; }
    public void setScreenshotPaths(String screenshotPaths) { this.screenshotPaths = screenshotPaths; }
    public void setScreenshotPaths(java.util.List<String> screenshotPathsList) { 
        this.screenshotPaths = screenshotPathsList != null ? String.join(",", screenshotPathsList) : null; 
    }

    public String getRequestResponseData() { return requestResponseData; }
    public void setRequestResponseData(String requestResponseData) { this.requestResponseData = requestResponseData; }

    public enum ExecutionStatus {
        PENDING, RUNNING, PASSED, FAILED, SKIPPED, ERROR
    }
}
