package org.example.service;

import org.springframework.stereotype.Service;

// Temporarily disabled - using new ReportService instead
@Service
public class ReportingService {
    public static class ReportResult {
        private String batchId;
        private String htmlReportPath;

        public String getBatchId() { return batchId; }
        public void setBatchId(String batchId) { this.batchId = batchId; }
        
        public String getHtmlReportPath() { return htmlReportPath; }
        public void setHtmlReportPath(String htmlReportPath) { this.htmlReportPath = htmlReportPath; }
    }
}
