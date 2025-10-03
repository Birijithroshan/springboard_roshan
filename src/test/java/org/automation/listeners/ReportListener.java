package org.automation.listeners;

import org.automation.reports.CsvReportGenerator;
import org.automation.reports.ExcelReportGenerator;
import org.automation.reports.HtmlReportGenerator;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class ReportListener implements ISuiteListener {

    @Override
    public void onFinish(ISuite suite) {
        try {
            System.out.println("[ReportListener] ✅ Generating reports...");
            HtmlReportGenerator.generateReport();
            CsvReportGenerator.generateReport();
            ExcelReportGenerator.generateReport();
            System.out.println("[ReportListener] 📊 Reports generated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

