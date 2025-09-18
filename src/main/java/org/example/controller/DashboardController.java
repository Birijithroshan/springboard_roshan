package org.example.controller;

import org.example.service.MockDataService;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private TestBatchRepository testBatchRepository;

    @Autowired
    private TestExecutionRepository testExecutionRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private TestScheduleRepository testScheduleRepository;

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Test Case Metrics
        long totalTestCases = testCaseRepository.count();
        long activeTestCases = testCaseRepository.findByIsActiveTrue().size();

        // Batch Metrics
        long totalBatches = testBatchRepository.count();
        long completedBatches = testBatchRepository.findByStatusOrderByCreatedAtDesc(
            org.example.model.TestBatch.BatchStatus.COMPLETED).size();

        // Execution Metrics
        long totalExecutions = testExecutionRepository.count();
        long passedExecutions = testExecutionRepository.findByStatus(
            org.example.model.TestExecution.ExecutionStatus.PASSED).size();

        // Schedule Metrics
        long totalSchedules = testScheduleRepository.count();
        long activeSchedules = testScheduleRepository.findByIsActiveTrue().size();

        // Calculate success rate
        double successRate = totalExecutions > 0 ? (double) passedExecutions / totalExecutions * 100 : 0;

        metrics.put("testCases", Map.of(
            "total", totalTestCases,
            "active", activeTestCases,
            "inactive", totalTestCases - activeTestCases
        ));

        metrics.put("batches", Map.of(
            "total", totalBatches,
            "completed", completedBatches,
            "pending", totalBatches - completedBatches
        ));

        metrics.put("executions", Map.of(
            "total", totalExecutions,
            "passed", passedExecutions,
            "failed", totalExecutions - passedExecutions,
            "successRate", Math.round(successRate * 100.0) / 100.0
        ));

        metrics.put("schedules", Map.of(
            "total", totalSchedules,
            "active", activeSchedules,
            "inactive", totalSchedules - activeSchedules
        ));

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<Map<String, Object>> getRecentActivity() {
        Map<String, Object> activity = new HashMap<>();

        // Get recent batches
        var recentBatches = testBatchRepository.findTopRecentBatches(5);

        // Get recent executions
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        var recentExecutions = testExecutionRepository.findByDateRange(last24Hours, LocalDateTime.now());

        activity.put("recentBatches", recentBatches);
        activity.put("recentExecutions", recentExecutions.size());
        activity.put("last24Hours", Map.of(
            "totalExecutions", recentExecutions.size(),
            "timestamp", LocalDateTime.now()
        ));

        return ResponseEntity.ok(activity);
    }

    @GetMapping("/performance-summary")
    public ResponseEntity<Map<String, Object>> getPerformanceSummary() {
        Map<String, Object> performance = new HashMap<>();

        // Mock performance data for demo
        performance.put("avgExecutionTime", "45.2 seconds");
        performance.put("parallelEfficiency", "87%");
        performance.put("resourceUtilization", "65%");
        performance.put("systemHealth", "Excellent");

        performance.put("trends", Map.of(
            "dailySuccessRate", 86.7,
            "weeklyTrend", "+2.3%",
            "monthlyImprovement", "+5.1%"
        ));

        performance.put("thresholds", Map.of(
            "responseTime", "< 2000ms",
            "errorRate", "< 2%",
            "availability", "> 99.5%"
        ));

        return ResponseEntity.ok(performance);
    }

    @GetMapping("/environment-status")
    public ResponseEntity<Map<String, Object>> getEnvironmentStatus() {
        Map<String, Object> envStatus = new HashMap<>();

        // Mock environment data
        envStatus.put("production", Map.of(
            "status", "Healthy",
            "lastRun", "2 hours ago",
            "successRate", "94.2%",
            "issues", 0
        ));

        envStatus.put("staging", Map.of(
            "status", "Healthy",
            "lastRun", "30 minutes ago",
            "successRate", "88.9%",
            "issues", 1
        ));

        envStatus.put("test", Map.of(
            "status", "Warning",
            "lastRun", "5 minutes ago",
            "successRate", "82.1%",
            "issues", 3
        ));

        return ResponseEntity.ok(envStatus);
    }
}
