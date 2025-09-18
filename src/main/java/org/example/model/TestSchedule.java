package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_name", nullable = false)
    private String scheduleName;

    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;

    @Column(name = "test_suite")
    private String testSuite;

    @Column(name = "environment")
    private String environment;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "parallel_threads")
    private Integer parallelThreads = 1;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_execution")
    private LocalDateTime lastExecution;

    @Column(name = "next_execution")
    private LocalDateTime nextExecution;

    @Column(name = "notification_emails")
    private String notificationEmails;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Explicit getters and setters to fix Lombok issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getScheduleName() { return scheduleName; }
    public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }

    // Alias method for getName() - returns scheduleName
    public String getName() { return scheduleName; }
    public void setName(String name) { this.scheduleName = name; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public String getTestSuite() { return testSuite; }
    public void setTestSuite(String testSuite) { this.testSuite = testSuite; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getParallelThreads() { return parallelThreads; }
    public void setParallelThreads(Integer parallelThreads) { this.parallelThreads = parallelThreads; }

    public Boolean getNotificationEnabled() { return notificationEnabled; }
    public void setNotificationEnabled(Boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }

    public String getNotificationEmails() { return notificationEmails; }
    public void setNotificationEmails(String notificationEmails) { this.notificationEmails = notificationEmails; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastExecution() { return lastExecution; }
    public void setLastExecution(LocalDateTime lastExecution) { this.lastExecution = lastExecution; }

    public LocalDateTime getNextExecution() { return nextExecution; }
    public void setNextExecution(LocalDateTime nextExecution) { this.nextExecution = nextExecution; }
}
