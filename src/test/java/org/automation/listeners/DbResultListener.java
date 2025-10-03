package org.automation.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.sql.*;

public class DbResultListener implements ITestListener {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/automation_tests?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Ck@709136"; // move to env var in prod

    private Connection getConn() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        persist(result, "PASS");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        persist(result, "FAIL");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        persist(result, "SKIPPED");
    }

    private void persist(ITestResult result, String status) {
        String insert = "INSERT INTO execution_logs " +
                "(test_type, us_id, test_case_id, message, level, log_time, tc_id, screenshot_path, start_time, end_time, duration) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?)";

        String testType = result.getTestContext().getSuite().getName().contains("UI") ? "UI" : "API";
        String usId = result.getAttribute("usId") != null ? result.getAttribute("usId").toString() : null;
        String testCaseId = result.getAttribute("testCaseId") != null ? result.getAttribute("testCaseId").toString() : null;
        String message = result.getThrowable() != null ? result.getThrowable().toString() : "Test " + status;
        String level = status.equals("FAIL") ? "ERROR" : "INFO";
        String tcId = result.getMethod().getMethodName();
        String screenshotPath = result.getAttribute("screenshotPath") != null ? result.getAttribute("screenshotPath").toString() : null;
        Timestamp startTime = new Timestamp(result.getStartMillis());
        Timestamp endTime = new Timestamp(result.getEndMillis());
        long duration = Math.max(0, result.getEndMillis() - result.getStartMillis());

        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, testType);
            ps.setString(2, usId);
            ps.setString(3, testCaseId);
            ps.setString(4, message);
            ps.setString(5, level);
            ps.setString(6, tcId);
            ps.setString(7, screenshotPath);
            ps.setTimestamp(8, startTime);
            ps.setTimestamp(9, endTime);
            ps.setLong(10, duration);

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to persist test result to DB: " + e.getMessage());
        }
    }

    @Override public void onStart(ITestContext context) { }
    @Override public void onFinish(ITestContext context) { }
    @Override public void onTestStart(ITestResult result) { }
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) { }
}
