package org.automation.analytics.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "execution_log")
public class ExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_name")
    private String testName;

    @Column(name = "test_class")
    private String testClass;

    @Column(name = "status")
    private String status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private long durationMs;

    @Column(name = "screenshot_path")
    private String screenshotPath;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "browser")
    private String browser;

    @Column(name = "suite_id")
    private String suiteId;

    @Column(name = "test_suite")
    private String testSuite;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "duration")
    private Long duration;

    // Getters
    public Long getId() { return id; }
    public String getTestName() { return testName; }
    public String getTestClass() { return testClass; }
    public String getStatus() { return status; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public long getDurationMs() { return durationMs; }
    public String getScreenshotPath() { return screenshotPath; }
    public String getErrorMessage() { return errorMessage; }
    public String getBrowser() { return browser; }
    public String getSuiteId() { return suiteId; }
    public String getTestSuite() { return testSuite; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Long getDuration() { return duration; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTestName(String testName) { this.testName = testName; }
    public void setTestClass(String testClass) { this.testClass = testClass; }
    public void setStatus(String status) { this.status = status; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setBrowser(String browser) { this.browser = browser; }
    public void setSuiteId(String suiteId) { this.suiteId = suiteId; }
    public void setTestSuite(String testSuite) { this.testSuite = testSuite; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setDuration(Long duration) { this.duration = duration; }
}
