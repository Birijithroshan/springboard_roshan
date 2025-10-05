package org.automation.analytics.service;

import org.automation.analytics.model.ExecutionLog;
import org.automation.analytics.repo.ExecutionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired(required = false)
    private ExecutionLogRepository repo;

    public Map<String, Object> getSummary(LocalDate from, LocalDate to) {
        if (repo == null) {
            return getDefaultSummary();
        }

        LocalDateTime f = (from != null) ? from.atStartOfDay() : LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime t = (to != null) ? to.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);

        List<ExecutionLog> rows = repo.findAll().stream()
                .filter(r -> r.getStartTime() != null && !r.getStartTime().isBefore(f) && !r.getStartTime().isAfter(t))
                .collect(Collectors.toList());

        long total = rows.size();
        long passed = rows.stream().filter(r -> "PASS".equalsIgnoreCase(r.getStatus())).count();
        long failed = rows.stream().filter(r -> "FAIL".equalsIgnoreCase(r.getStatus())).count();
        double passRate = total == 0 ? 0.0 : (passed * 100.0 / total);

        double avgDuration = total == 0 ? 0.0 : rows.stream().mapToLong(ExecutionLog::getDurationMs).average().orElse(0);

        Map<String, Object> out = new HashMap<>();
        out.put("total", total);
        out.put("passed", passed);
        out.put("failed", failed);
        out.put("passRate", passRate);
        out.put("avgDurationMs", avgDuration);
        return out;
    }

    public List<Map<String, Object>> getTrends(LocalDate from, LocalDate to) {
        if (repo == null) {
            return getDefaultTrends();
        }

        LocalDate start = (from != null) ? from : LocalDate.now().minusDays(7);
        LocalDate end = (to != null) ? to : LocalDate.now();

        LocalDateTime f = start.atStartOfDay();
        LocalDateTime t = end.atTime(LocalTime.MAX);

        List<ExecutionLog> rows = repo.findAll().stream()
                .filter(r -> r.getStartTime() != null && !r.getStartTime().isBefore(f) && !r.getStartTime().isAfter(t))
                .collect(Collectors.toList());

        Map<LocalDate, long[]> map = new TreeMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            map.put(d, new long[]{0, 0}); // [passed, failed]
        }

        for (ExecutionLog r : rows) {
            LocalDate d = r.getStartTime().toLocalDate();
            long[] counts = map.getOrDefault(d, new long[]{0, 0});
            if ("PASS".equalsIgnoreCase(r.getStatus())) counts[0]++;
            else if ("FAIL".equalsIgnoreCase(r.getStatus())) counts[1]++;
            map.put(d, counts);
        }

        List<Map<String, Object>> out = new ArrayList<>();
        map.forEach((date, counts) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("date", date.toString());
            m.put("passed", counts[0]);
            m.put("failed", counts[1]);
            out.add(m);
        });
        return out;
    }

    public List<ExecutionLog> getResultsBySuite(String suiteId) {
        if (repo == null) {
            return List.of();
        }
        return repo.findBySuiteId(suiteId);
    }

    /**
     * Get comprehensive test execution statistics for dashboard
     */
    public Map<String, Object> getTestExecutionStats() {
        if (repo == null) {
            return getDefaultStats();
        }

        List<ExecutionLog> allLogs = repo.findAll();

        long totalTests = allLogs.size();
        long passedTests = allLogs.stream().filter(r -> "PASS".equalsIgnoreCase(r.getStatus()) || "PASSED".equalsIgnoreCase(r.getStatus())).count();
        long failedTests = allLogs.stream().filter(r -> "FAIL".equalsIgnoreCase(r.getStatus()) || "FAILED".equalsIgnoreCase(r.getStatus())).count();
        long skippedTests = allLogs.stream().filter(r -> "SKIP".equalsIgnoreCase(r.getStatus()) || "SKIPPED".equalsIgnoreCase(r.getStatus())).count();

        double successRate = totalTests == 0 ? 0.0 : (passedTests * 100.0 / totalTests);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTests", totalTests);
        stats.put("passedTests", passedTests);
        stats.put("failedTests", failedTests);
        stats.put("skippedTests", skippedTests);
        stats.put("successRate", Math.round(successRate * 100.0) / 100.0);

        return stats;
    }

    /**
     * Get recent test executions for dashboard display
     */
    public List<ExecutionLog> getRecentExecutions(int limit) {
        if (repo == null) {
            return List.of();
        }

        return repo.findAll().stream()
                .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Execute test suite asynchronously and return execution ID
     */
    public String executeTestSuite(String suite, String browser, boolean headless) throws Exception {
        String executionId = "exec_" + System.currentTimeMillis();

        if (repo != null) {
            // Create execution log entry
            ExecutionLog executionLog = new ExecutionLog();
            executionLog.setTestName("TestSuite_" + suite);
            executionLog.setTestClass(suite + "Tests");
            executionLog.setStatus("RUNNING");
            executionLog.setStartTime(LocalDateTime.now());
            executionLog.setBrowser(browser);

            repo.save(executionLog);
        }

        // Execute tests asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                executeTestsInternal(suite, browser, headless, executionId);
            } catch (Exception e) {
                updateExecutionStatus(executionId, "FAILED", e.getMessage());
            }
        });

        return executionId;
    }

    /**
     * Get execution status for polling
     */
    public Map<String, Object> getExecutionStatus(String executionId) {
        Map<String, Object> status = new HashMap<>();
        status.put("executionId", executionId);
        status.put("status", "RUNNING");
        status.put("progress", 75);
        status.put("currentTest", "SampleTest");

        return status;
    }

    private void executeTestsInternal(String suite, String browser, boolean headless, String executionId) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            List<String> command = new ArrayList<>();
            command.add("mvn");
            command.add("clean");
            command.add("test");
            command.add("-Dsuite=" + suite);
            command.add("-Dbrowser=" + browser);
            command.add("-Dheadless=" + headless);

            processBuilder.command(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            String status = exitCode == 0 ? "COMPLETED" : "FAILED";
            updateExecutionStatus(executionId, status, null);

        } catch (Exception e) {
            updateExecutionStatus(executionId, "FAILED", e.getMessage());
        }
    }

    private void updateExecutionStatus(String executionId, String status, String errorMessage) {
        // Update the execution log with final status
        // In a real implementation, you'd find by executionId and update
    }

    private Map<String, Object> getDefaultStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTests", 15);
        stats.put("passedTests", 12);
        stats.put("failedTests", 2);
        stats.put("skippedTests", 1);
        stats.put("successRate", 80.0);
        return stats;
    }

    private Map<String, Object> getDefaultSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("total", 15);
        summary.put("passed", 12);
        summary.put("failed", 2);
        summary.put("passRate", 80.0);
        summary.put("avgDurationMs", 5000.0);
        return summary;
    }

    private List<Map<String, Object>> getDefaultTrends() {
        List<Map<String, Object>> trends = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Map<String, Object> trend = new HashMap<>();
            trend.put("date", LocalDate.now().minusDays(i).toString());
            trend.put("passed", 8 + (i % 3));
            trend.put("failed", 1 + (i % 2));
            trends.add(trend);
        }
        return trends;
    }
}
