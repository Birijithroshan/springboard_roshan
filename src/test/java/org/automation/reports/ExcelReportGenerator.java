package org.automation.reports;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExcelReportGenerator {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/automation_tests";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Ck@709136";

    public static void generateReport() throws Exception {
        String fileName = "artifacts/reports/TestReport_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        try (Workbook workbook = new XSSFWorkbook();
             Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM execution_log")) {

            Sheet sheet = workbook.createSheet("Execution Log");
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "TestName", "Status", "Type", "US_ID", "TC_ID", "Artifact", "ExecutionTime"};
            for (int i = 0; i < columns.length; i++) header.createCell(i).setCellValue(columns[i]);

            int rowNum = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rs.getInt("id"));
                row.createCell(1).setCellValue(rs.getString("test_name"));
                row.createCell(2).setCellValue(rs.getString("status"));
                row.createCell(3).setCellValue(rs.getString("test_type"));
                row.createCell(4).setCellValue(rs.getString("us_id"));
                row.createCell(5).setCellValue(rs.getString("tc_id"));
                row.createCell(6).setCellValue(rs.getString("artifact"));
                row.createCell(7).setCellValue(rs.getString("execution_time"));
            }

            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                workbook.write(fos);
            }
        }

        System.out.println("Excel report generated: " + fileName);
    }

    // ✅ Wrapper method for compatibility
    public static void generateExcelReport() throws Exception {
        generateReport();
    }
}
