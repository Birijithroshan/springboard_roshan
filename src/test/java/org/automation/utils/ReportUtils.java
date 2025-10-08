package org.automation.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportUtils {

    private static ExtentReports extent;
    private static ExtentTest test;

    // Centralized timestamp for all reports in a run
    private static final String TIMESTAMP = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

    public static String getTimestamp() {
        return TIMESTAMP;
    }

    // Initialize ExtentReports
    public static ExtentReports initReports() {
        if (extent == null) {
            String reportPath = System.getProperty("user.dir") + "/reports/AutomationReport_" + TIMESTAMP + ".html";
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("User", System.getProperty("user.name"));
        }
        return extent;
    }

    public static ExtentTest createTest(String testName, String description) {
        test = extent.createTest(testName, description);
        return test;
    }

    public static void flushReports() {
        if (extent != null) extent.flush();
    }
}
