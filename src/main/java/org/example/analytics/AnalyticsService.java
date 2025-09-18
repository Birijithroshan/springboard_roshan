package org.example.analytics;

import org.example.model.TestExecution;
import org.example.repository.TestExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    @Autowired
    private TestExecutionRepository testExecutionRepository;

    public TestTrendAnalysis generateTrendAnalysis(LocalDateTime fromDate, LocalDateTime toDate) {
        List<TestExecution> executions = testExecutionRepository.findByDateRange(fromDate, toDate);

        TestTrendAnalysis analysis = new TestTrendAnalysis();
        analysis.setFromDate(fromDate);
        analysis.setToDate(toDate);
        analysis.setTotalExecutions(executions.size());

        // Calculate pass/fail rates
        Map<TestExecution.ExecutionStatus, Long> statusCounts = executions.stream()
                .collect(Collectors.groupingBy(TestExecution::getStatus, Collectors.counting()));

        long passedCount = statusCounts.getOrDefault(TestExecution.ExecutionStatus.PASSED, 0L);
        long failedCount = statusCounts.getOrDefault(TestExecution.ExecutionStatus.FAILED, 0L);
        long errorCount = statusCounts.getOrDefault(TestExecution.ExecutionStatus.ERROR, 0L);

        analysis.setPassedTests(passedCount);
        analysis.setFailedTests(failedCount);
        analysis.setErrorTests(errorCount);

        if (executions.size() > 0) {
            analysis.setPassRate((double) passedCount / executions.size() * 100);
            analysis.setFailRate((double) (failedCount + errorCount) / executions.size() * 100);
        }

        // Calculate average execution time
        double avgDuration = executions.stream()
                .filter(e -> e.getExecutionDuration() != null)
                .mapToLong(TestExecution::getExecutionDuration)
                .average()
                .orElse(0.0);
        analysis.setAverageExecutionTime(avgDuration);

        // Top failing tests
        Map<String, Long> failingTests = executions.stream()
                .filter(e -> e.getStatus() == TestExecution.ExecutionStatus.FAILED ||
                           e.getStatus() == TestExecution.ExecutionStatus.ERROR)
                .collect(Collectors.groupingBy(e -> e.getTestCase().getName(), Collectors.counting()));

        analysis.setTopFailingTests(failingTests.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                )));

        // Environment-wise statistics
        Map<String, Long> envStats = executions.stream()
                .collect(Collectors.groupingBy(TestExecution::getEnvironment, Collectors.counting()));
        analysis.setEnvironmentStats(envStats);

        log.info("Generated trend analysis for period {} to {}", fromDate, toDate);
        return analysis;
    }

    public RegressionMetrics calculateRegressionMetrics(String environment, int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        LocalDateTime toDate = LocalDateTime.now();

        List<TestExecution> executions = testExecutionRepository.findByDateRange(fromDate, toDate)
                .stream()
                .filter(e -> environment.equals(e.getEnvironment()))
                .collect(Collectors.toList());

        RegressionMetrics metrics = new RegressionMetrics();
        metrics.setEnvironment(environment);
        metrics.setDays(days);

        if (executions.isEmpty()) {
            return metrics;
        }

        // Calculate stability metrics
        long totalTests = executions.size();
        long stableTests = executions.stream()
                .collect(Collectors.groupingBy(e -> e.getTestCase().getId(), Collectors.counting()))
                .values().stream()
                .mapToLong(count -> count > 1 ? 1 : 0)
                .sum();

        metrics.setStabilityScore((double) stableTests / totalTests * 100);

        // Calculate regression detection rate
        long regressionTests = executions.stream()
                .filter(e -> e.getStatus() == TestExecution.ExecutionStatus.FAILED)
                .collect(Collectors.groupingBy(e -> e.getTestCase().getId(), Collectors.counting()))
                .size();

        metrics.setRegressionDetectionRate((double) regressionTests / totalTests * 100);

        // Calculate execution efficiency
        double avgExecutionTime = executions.stream()
                .filter(e -> e.getExecutionDuration() != null)
                .mapToLong(TestExecution::getExecutionDuration)
                .average()
                .orElse(0.0);

        metrics.setAverageExecutionTime(avgExecutionTime);
        metrics.setTotalExecutions(totalTests);

        log.info("Calculated regression metrics for environment {} over {} days", environment, days);
        return metrics;
    }

    @Data
    public static class TestTrendAnalysis {
        private LocalDateTime fromDate;
        private LocalDateTime toDate;
        private long totalExecutions;
        private long passedTests;
        private long failedTests;
        private long errorTests;
        private double passRate;
        private double failRate;
        private double averageExecutionTime;
        private Map<String, Long> topFailingTests;
        private Map<String, Long> environmentStats;

        // Explicit getters and setters
        public LocalDateTime getFromDate() { return fromDate; }
        public void setFromDate(LocalDateTime fromDate) { this.fromDate = fromDate; }

        public LocalDateTime getToDate() { return toDate; }
        public void setToDate(LocalDateTime toDate) { this.toDate = toDate; }

        public long getTotalExecutions() { return totalExecutions; }
        public void setTotalExecutions(long totalExecutions) { this.totalExecutions = totalExecutions; }

        public long getPassedTests() { return passedTests; }
        public void setPassedTests(long passedTests) { this.passedTests = passedTests; }

        public long getFailedTests() { return failedTests; }
        public void setFailedTests(long failedTests) { this.failedTests = failedTests; }

        public long getErrorTests() { return errorTests; }
        public void setErrorTests(long errorTests) { this.errorTests = errorTests; }

        public double getPassRate() { return passRate; }
        public void setPassRate(double passRate) { this.passRate = passRate; }

        public double getFailRate() { return failRate; }
        public void setFailRate(double failRate) { this.failRate = failRate; }

        public double getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(double averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }

        public Map<String, Long> getTopFailingTests() { return topFailingTests; }
        public void setTopFailingTests(Map<String, Long> topFailingTests) { this.topFailingTests = topFailingTests; }

        public Map<String, Long> getEnvironmentStats() { return environmentStats; }
        public void setEnvironmentStats(Map<String, Long> environmentStats) { this.environmentStats = environmentStats; }
    }

    @Data
    public static class RegressionMetrics {
        private String environment;
        private int days;
        private double stabilityScore;
        private double regressionDetectionRate;
        private double averageExecutionTime;
        private long totalExecutions;

        // Explicit getters and setters
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }

        public int getDays() { return days; }
        public void setDays(int days) { this.days = days; }

        public double getStabilityScore() { return stabilityScore; }
        public void setStabilityScore(double stabilityScore) { this.stabilityScore = stabilityScore; }

        public double getRegressionDetectionRate() { return regressionDetectionRate; }
        public void setRegressionDetectionRate(double regressionDetectionRate) { this.regressionDetectionRate = regressionDetectionRate; }

        public double getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(double averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }

        public long getTotalExecutions() { return totalExecutions; }
        public void setTotalExecutions(long totalExecutions) { this.totalExecutions = totalExecutions; }
    }
}
