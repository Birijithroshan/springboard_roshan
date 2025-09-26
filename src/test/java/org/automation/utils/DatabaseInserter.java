package org.automation.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseInserter {

    // MySQL credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/automation_tests";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Ck@709136";

    // ---------- Insert UI Test Result ----------
    public static void insertUiTestResult(String usId, String testCaseId, String name,
                                          String status, long durationMs, String artifact) {
        String sql = "INSERT INTO ui_tests (us_id, test_case_id, name, status, execution_time, duration_ms, artifact) " +
                "VALUES (?, ?, ?, ?, NOW(), ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usId);
            stmt.setString(2, testCaseId);
            stmt.setString(3, name);
            stmt.setString(4, status);
            stmt.setLong(5, durationMs);
            stmt.setString(6, artifact);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------- Insert API Test Result ----------
    public static void insertApiTestResult(String usId, String testCaseId, String name,
                                           String status, long durationMs, String request, String response, String artifact) {
        String sql = "INSERT INTO api_responses (us_id, test_case_id, name, status, execution_time, duration_ms, request, response, artifact) " +
                "VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usId);
            stmt.setString(2, testCaseId);
            stmt.setString(3, name);
            stmt.setString(4, status);
            stmt.setLong(5, durationMs);
            stmt.setString(6, request);
            stmt.setString(7, response);
            stmt.setString(8, artifact);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------- Insert Execution Log (✅ now includes screenshot path) ----------
    public static void insertExecutionLog(String testType, String usId, String testCaseId,
                                          String message, String level, String screenshotPath) {
        String sql = "INSERT INTO execution_logs (test_type, us_id, test_case_id, message, level, screenshot_path, log_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, testType);
            stmt.setString(2, usId);
            stmt.setString(3, testCaseId);
            stmt.setString(4, message);
            stmt.setString(5, level);
            stmt.setString(6, screenshotPath);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------- Update screenshot path for a test after capture (optional helper) ----------
    public static void updateScreenshotPath(String testName, String screenshotPath) {
        String sql = "UPDATE execution_logs SET screenshot_path = ? WHERE test_name = ? ORDER BY execution_time DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, screenshotPath);
            stmt.setString(2, testName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
