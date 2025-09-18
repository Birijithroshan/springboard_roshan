package org.example.reporting;

import org.example.model.TestExecution;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerator.class);

    private static final String REPORT_DIRECTORY = "test-reports/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public String generateHTMLReport(List<TestExecution> executions, String reportType) throws IOException {
        String reportName = String.format("%s_TestReport_%s.html", 
                reportType, 
                new Date().toInstant().toString().replaceAll("[:\\.]", "-"));
        
        String reportPath = REPORT_DIRECTORY + reportName;
        ensureDirectoryExists(REPORT_DIRECTORY);

        StringBuilder html = new StringBuilder();
        html.append(getHTMLHeader(reportType));
        html.append(generateExecutionSummary(executions));
        html.append(generateDetailedResults(executions));
        html.append(generateFailureAnalysis(executions));
        html.append(getHTMLFooter());

        try (FileWriter writer = new FileWriter(reportPath)) {
            writer.write(html.toString());
        }

        log.info("HTML report generated: {}", reportPath);
        return reportPath;
    }

    public String generateCSVReport(List<TestExecution> executions, String reportType) throws IOException {
        String reportName = String.format("%s_TestReport_%s.csv", 
                reportType, 
                new Date().toInstant().toString().replaceAll("[:\\.]", "-"));
        
        String reportPath = REPORT_DIRECTORY + reportName;
        ensureDirectoryExists(REPORT_DIRECTORY);

        StringBuilder csv = new StringBuilder();
        csv.append("Test Name,Test Type,Status,Duration (ms),Environment,Start Time,End Time,Error Message\n");

        for (TestExecution execution : executions) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    execution.getTestCase().getName(),
                    execution.getTestCase().getTestType(),
                    execution.getStatus(),
                    execution.getExecutionDuration() != null ? execution.getExecutionDuration() : 0,
                    execution.getEnvironment(),
                    execution.getStartTime() != null ? execution.getStartTime().format(DATE_FORMATTER) : "",
                    execution.getEndTime() != null ? execution.getEndTime().format(DATE_FORMATTER) : "",
                    execution.getErrorMessage() != null ? execution.getErrorMessage().replace("\"", "\"\"") : ""));
        }

        try (FileWriter writer = new FileWriter(reportPath)) {
            writer.write(csv.toString());
        }

        log.info("CSV report generated: {}", reportPath);
        return reportPath;
    }

    public String generateXMLReport(List<TestExecution> executions, String reportType) throws IOException {
        String reportName = String.format("%s_TestReport_%s.xml", 
                reportType, 
                new Date().toInstant().toString().replaceAll("[:\\.]", "-"));
        
        String reportPath = REPORT_DIRECTORY + reportName;
        ensureDirectoryExists(REPORT_DIRECTORY);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<testsuites>\n");
        
        Map<String, List<TestExecution>> groupedByType = executions.stream()
                .collect(Collectors.groupingBy(e -> e.getTestCase().getTestType().toString()));

        for (Map.Entry<String, List<TestExecution>> entry : groupedByType.entrySet()) {
            xml.append(generateTestSuiteXML(entry.getKey(), entry.getValue()));
        }

        xml.append("</testsuites>\n");

        try (FileWriter writer = new FileWriter(reportPath)) {
            writer.write(xml.toString());
        }

        log.info("XML report generated: {}", reportPath);
        return reportPath;
    }

    public String generateAllureReport(List<TestExecution> executions, String reportType) throws IOException {
        // For now, generate a simplified Allure-compatible JSON
        String reportName = String.format("%s_AllureResults_%s.json", 
                reportType, 
                new Date().toInstant().toString().replaceAll("[:\\.]", "-"));
        
        String reportPath = REPORT_DIRECTORY + reportName;
        ensureDirectoryExists(REPORT_DIRECTORY);

        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < executions.size(); i++) {
            TestExecution execution = executions.get(i);
            json.append(generateAllureTestCase(execution));
            if (i < executions.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]\n");

        try (FileWriter writer = new FileWriter(reportPath)) {
            writer.write(json.toString());
        }

        log.info("Allure JSON report generated: {}", reportPath);
        return reportPath;
    }

    private String getHTMLHeader(String reportType) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s Test Report</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
                    .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    h1 { color: #333; text-align: center; margin-bottom: 30px; border-bottom: 3px solid #007acc; padding-bottom: 10px; }
                    h2 { color: #444; border-left: 4px solid #007acc; padding-left: 15px; margin-top: 30px; }
                    .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 20px 0; }
                    .summary-card { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 20px; border-radius: 8px; text-align: center; }
                    .summary-card h3 { margin: 0 0 10px 0; font-size: 2.5em; }
                    .summary-card p { margin: 0; opacity: 0.9; }
                    table { width: 100%%; border-collapse: collapse; margin: 20px 0; background: white; }
                    th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
                    th { background-color: #f8f9fa; font-weight: 600; }
                    tr:hover { background-color: #f5f5f5; }
                    .status-passed { color: #28a745; font-weight: bold; }
                    .status-failed { color: #dc3545; font-weight: bold; }
                    .status-running { color: #ffc107; font-weight: bold; }
                    .error-details { background: #f8f9fa; border-left: 4px solid #dc3545; padding: 15px; margin: 10px 0; border-radius: 4px; }
                    .logs { background: #f8f9fa; border: 1px solid #ddd; padding: 15px; border-radius: 4px; font-family: monospace; white-space: pre-wrap; max-height: 300px; overflow-y: auto; }
                    .screenshot-link { color: #007acc; text-decoration: none; }
                    .screenshot-link:hover { text-decoration: underline; }
                    .footer { text-align: center; margin-top: 40px; color: #666; border-top: 1px solid #eee; padding-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>%s Test Execution Report</h1>
                    <p style="text-align: center; color: #666; margin-bottom: 30px;">Generated on: %s</p>
            """, reportType, reportType, new Date().toString());
    }

    private String generateExecutionSummary(List<TestExecution> executions) {
        long total = executions.size();
        long passed = executions.stream().mapToLong(e -> e.getStatus() == TestExecution.ExecutionStatus.PASSED ? 1 : 0).sum();
        long failed = executions.stream().mapToLong(e -> e.getStatus() == TestExecution.ExecutionStatus.FAILED ? 1 : 0).sum();
        long running = executions.stream().mapToLong(e -> e.getStatus() == TestExecution.ExecutionStatus.RUNNING ? 1 : 0).sum();
        
        double passRate = total > 0 ? (double) passed / total * 100 : 0;

        return String.format("""
            <h2>Execution Summary</h2>
            <div class="summary">
                <div class="summary-card">
                    <h3>%d</h3>
                    <p>Total Tests</p>
                </div>
                <div class="summary-card">
                    <h3>%d</h3>
                    <p>Passed</p>
                </div>
                <div class="summary-card">
                    <h3>%d</h3>
                    <p>Failed</p>
                </div>
                <div class="summary-card">
                    <h3>%.1f%%</h3>
                    <p>Pass Rate</p>
                </div>
            </div>
            """, total, passed, failed, passRate);
    }

    private String generateDetailedResults(List<TestExecution> executions) {
        StringBuilder html = new StringBuilder();
        html.append("<h2>Detailed Test Results</h2>");
        html.append("<table>");
        html.append("<tr><th>Test Name</th><th>Type</th><th>Status</th><th>Duration</th><th>Environment</th><th>Start Time</th><th>Details</th></tr>");

        for (TestExecution execution : executions) {
            String statusClass = "status-" + execution.getStatus().toString().toLowerCase();
            html.append(String.format("""
                <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td class="%s">%s</td>
                    <td>%d ms</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>
                        %s
                        %s
                        %s
                    </td>
                </tr>
                """,
                execution.getTestCase().getName(),
                execution.getTestCase().getTestType(),
                statusClass,
                execution.getStatus(),
                execution.getExecutionDuration() != null ? execution.getExecutionDuration() : 0,
                execution.getEnvironment(),
                execution.getStartTime() != null ? execution.getStartTime().format(DATE_FORMATTER) : "N/A",
                execution.getErrorMessage() != null ? "<div class='error-details'>Error: " + execution.getErrorMessage() + "</div>" : "",
                execution.getExecutionLogs() != null ? "<div class='logs'>" + execution.getExecutionLogs() + "</div>" : "",
                execution.getScreenshotPaths() != null && !execution.getScreenshotPaths().isEmpty() ? 
                    "<div>Screenshots: " + String.join(", ", execution.getScreenshotPaths()) + "</div>" : ""));
        }

        html.append("</table>");
        return html.toString();
    }

    private String generateFailureAnalysis(List<TestExecution> executions) {
        List<TestExecution> failures = executions.stream()
                .filter(e -> e.getStatus() == TestExecution.ExecutionStatus.FAILED)
                .collect(Collectors.toList());

        if (failures.isEmpty()) {
            return "<h2>Failure Analysis</h2><p>No failures detected! All tests passed successfully.</p>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>Failure Analysis</h2>");
        
        Map<String, Long> errorCounts = failures.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getErrorMessage() != null ? e.getErrorMessage() : "Unknown Error",
                    Collectors.counting()));

        html.append("<h3>Error Distribution</h3>");
        html.append("<ul>");
        for (Map.Entry<String, Long> entry : errorCounts.entrySet()) {
            html.append(String.format("<li><strong>%s</strong>: %d occurrences</li>", entry.getKey(), entry.getValue()));
        }
        html.append("</ul>");

        return html.toString();
    }

    private String getHTMLFooter() {
        return """
            <div class="footer">
                <p>Generated by Springboard Test Automation Framework</p>
            </div>
            </div>
            </body>
            </html>
            """;
    }

    private String generateTestSuiteXML(String testType, List<TestExecution> executions) {
        StringBuilder xml = new StringBuilder();
        long failures = executions.stream().mapToLong(e -> e.getStatus() == TestExecution.ExecutionStatus.FAILED ? 1 : 0).sum();
        
        xml.append(String.format("  <testsuite name=\"%s\" tests=\"%d\" failures=\"%d\" time=\"%.3f\">\n",
                testType, executions.size(), failures, 
                executions.stream().mapToLong(e -> e.getExecutionDuration() != null ? e.getExecutionDuration() : 0).sum() / 1000.0));

        for (TestExecution execution : executions) {
            xml.append(String.format("    <testcase name=\"%s\" classname=\"%s\" time=\"%.3f\"",
                    execution.getTestCase().getName(),
                    testType,
                    (execution.getExecutionDuration() != null ? execution.getExecutionDuration() : 0) / 1000.0));

            if (execution.getStatus() == TestExecution.ExecutionStatus.FAILED) {
                xml.append(">\n");
                xml.append(String.format("      <failure message=\"%s\">%s</failure>\n",
                        execution.getErrorMessage() != null ? execution.getErrorMessage() : "Test failed",
                        execution.getExecutionLogs() != null ? execution.getExecutionLogs() : ""));
                xml.append("    </testcase>\n");
            } else {
                xml.append(" />\n");
            }
        }

        xml.append("  </testsuite>\n");
        return xml.toString();
    }

    private String generateAllureTestCase(TestExecution execution) {
        return String.format("""
            {
                "uuid": "%s",
                "name": "%s",
                "fullName": "%s",
                "status": "%s",
                "start": %d,
                "stop": %d,
                "labels": [
                    {"name": "suite", "value": "%s"},
                    {"name": "testClass", "value": "%s"}
                ],
                "statusDetails": {
                    "message": "%s",
                    "trace": "%s"
                }
            }""",
            UUID.randomUUID().toString(),
            execution.getTestCase().getName(),
            execution.getTestCase().getName(),
            execution.getStatus().toString().toLowerCase(),
            execution.getStartTime() != null ? execution.getStartTime().toEpochSecond(java.time.ZoneOffset.UTC) * 1000 : 0,
            execution.getEndTime() != null ? execution.getEndTime().toEpochSecond(java.time.ZoneOffset.UTC) * 1000 : 0,
            execution.getTestCase().getTestType().toString(),
            execution.getTestCase().getTestType().toString(),
            execution.getErrorMessage() != null ? execution.getErrorMessage().replace("\"", "\\\"") : "",
            execution.getExecutionLogs() != null ? execution.getExecutionLogs().replace("\"", "\\\"") : "");
    }

    private void ensureDirectoryExists(String directory) {
        try {
            Files.createDirectories(Paths.get(directory));
        } catch (IOException e) {
            log.error("Failed to create directory: " + directory, e);
        }
    }
}
