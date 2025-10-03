//package org.automation.analytics.model;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "execution_log")
//public class ExecutionLog {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "test_name")
//    private String testName;
//
//    @Column(name = "status")
//    private String status;
//
//    @Column(name = "start_time")
//    private LocalDateTime startTime;
//
//    @Column(name = "end_time")
//    private LocalDateTime endTime;
//
//    @Column(name = "duration_ms")
//    private long durationMs;
//
//    @Column(name = "screenshot_path")
//    private String screenshotPath;
//
//    @Column(name = "error_message")
//    private String errorMessage;
//
//    // ✅ Add proper getters (and setters if needed)
//    public LocalDateTime getStartTime() {
//        return startTime;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public long getDurationMs() {
//        return durationMs;
//    }
//
//    public String getTestName() {
//        return testName;
//    }
//
//    public LocalDateTime getEndTime() {
//        return endTime;
//    }
//
//    public String getScreenshotPath() {
//        return screenshotPath;
//    }
//
//    public String getErrorMessage() {
//        return errorMessage;
//    }
//
//    // (optional) setters if you want JPA to set these fields
//}
