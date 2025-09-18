package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_batches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TestBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_name", nullable = false)
    private String batchName;

    @Column(name = "batch_id", nullable = false, unique = true)
    private String batchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_tests")
    private Integer totalTests = 0;

    @Column(name = "passed_tests")
    private Integer passedTests = 0;

    @Column(name = "failed_tests")
    private Integer failedTests = 0;

    @Column(name = "skipped_tests")
    private Integer skippedTests = 0;

    @Column(name = "environment")
    private String environment;

    @Column(name = "parallel_threads")
    private Integer parallelThreads = 1;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "testBatch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<TestExecution> testExecutions;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Explicit getters and setters to fix Lombok issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    // Alias method for setName() - delegates to setBatchName()
    public String getName() { return batchName; }
    public void setName(String name) { this.batchName = name; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getTotalTests() { return totalTests; }
    public void setTotalTests(Integer totalTests) { this.totalTests = totalTests; }

    public Integer getPassedTests() { return passedTests; }
    public void setPassedTests(Integer passedTests) { this.passedTests = passedTests; }

    public Integer getFailedTests() { return failedTests; }
    public void setFailedTests(Integer failedTests) { this.failedTests = failedTests; }

    public Integer getSkippedTests() { return skippedTests; }
    public void setSkippedTests(Integer skippedTests) { this.skippedTests = skippedTests; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public Integer getParallelThreads() { return parallelThreads; }
    public void setParallelThreads(Integer parallelThreads) { this.parallelThreads = parallelThreads; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<TestExecution> getTestExecutions() { return testExecutions; }
    public void setTestExecutions(List<TestExecution> testExecutions) { this.testExecutions = testExecutions; }

    public enum BatchStatus {
        SCHEDULED, RUNNING, COMPLETED, CANCELLED, FAILED
    }
}
