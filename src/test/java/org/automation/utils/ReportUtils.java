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
}
