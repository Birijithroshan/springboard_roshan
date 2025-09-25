package org.automation.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelUtils {

    private static Workbook workbook = new XSSFWorkbook();
    private static Sheet sheet = workbook.createSheet("Test Results");
    private static int rowCount = 0;

    public static void addResult(String testId, String testName, String status) {
        Row row = sheet.createRow(rowCount++);
        row.createCell(0).setCellValue(testId);
        row.createCell(1).setCellValue(testName);
        row.createCell(2).setCellValue(status);
    }

    public static void save(String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            workbook.write(fos);
            fos.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
