package org.automation.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportUtils {

    private static ExtentReports extent;
    private static ExtentTest test;

    // Initialize ExtentReports
    public static ExtentReports initReports() {
        if (extent == null) {
            // Generate timestamped report file
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String reportPath = System.getProperty("user.dir") + "/reports/AutomationReport_" + timestamp + ".html";

            // Create ExtentSparkReporter
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);

            // Initialize ExtentReports and attach reporter
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);

            // You can add system info
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("User", System.getProperty("user.name"));
        }
        return extent;
    }

    // Create a test entry in the report
    public static ExtentTest createTest(String testName, String description) {
        test = extent.createTest(testName, description);
        return test;
    }

    // Flush the report to write to file
    public static void flushReports() {
        if (extent != null) {
            extent.flush();
        }
    }

    // Generate API test summary report
    public static void generateApiTestSummary(org.testng.ITestContext context) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String summaryPath = "artifacts/api/api_summary_" + timestamp + ".json";

            // Create summary JSON
            StringBuilder summary = new StringBuilder();
            summary.append("{\n");
            summary.append("  \"suiteName\": \"").append(context.getSuite().getName()).append("\",\n");
            summary.append("  \"totalTests\": ").append(context.getAllTestMethods().length).append(",\n");
            summary.append("  \"passedTests\": ").append(context.getPassedTests().size()).append(",\n");
            summary.append("  \"failedTests\": ").append(context.getFailedTests().size()).append(",\n");
            summary.append("  \"skippedTests\": ").append(context.getSkippedTests().size()).append(",\n");
            summary.append("  \"timestamp\": \"").append(timestamp).append("\"\n");
            summary.append("}\n");

            // Write to file
            java.nio.file.Path path = java.nio.file.Paths.get(summaryPath);
            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.write(path, summary.toString().getBytes());

        } catch (Exception e) {
            System.err.println("Failed to generate API test summary: " + e.getMessage());
        }
    }

    // Save API test result to artifacts
    public static void saveApiTestResult(String testName, String jsonResult) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String resultPath = "artifacts/api/" + testName + "_" + timestamp + ".json";

            java.nio.file.Path path = java.nio.file.Paths.get(resultPath);
            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.write(path, jsonResult.getBytes());

        } catch (Exception e) {
            System.err.println("Failed to save API test result: " + e.getMessage());
        }
    }
}
