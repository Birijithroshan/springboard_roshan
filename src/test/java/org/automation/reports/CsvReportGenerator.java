package org.automation.reports;

import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvReportGenerator {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/automation_tests";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Ck@709136";

    public static void generateReport() throws Exception {
        String fileName = "artifacts/reports/TestReport_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        try (FileWriter writer = new FileWriter(fileName);
             Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM execution_log")) {

            // Write header
            writer.append("ID,TestName,Status,Type,US_ID,TC_ID,Artifact,ExecutionTime\n");

            while (rs.next()) {
                writer.append(rs.getInt("id") + ",")
                        .append(rs.getString("test_name") + ",")
                        .append(rs.getString("status") + ",")
                        .append(rs.getString("test_type") + ",")
                        .append(rs.getString("us_id") + ",")
                        .append(rs.getString("tc_id") + ",")
                        .append("\"" + rs.getString("artifact") + "\"" + ",")
                        .append(rs.getString("execution_time") + "\n");
            }
        }

        System.out.println("CSV report generated: " + fileName);
    }

    // ✅ Wrapper method for compatibility
    public static void generateCsvReport() throws Exception {
        generateReport();
    }
}
