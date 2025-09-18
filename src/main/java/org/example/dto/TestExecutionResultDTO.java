package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestExecutionResultDTO {
    private String batchId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private int skippedTests;
    private String environment;
    private String browser;
    private List<TestCaseResultDTO> testResults;
    private String reportPath;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getEndTime() {
        return endTime;
    }
}
