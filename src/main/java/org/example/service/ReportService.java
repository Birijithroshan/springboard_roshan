package org.example.service;

import org.example.model.TestExecution;
import org.example.reporting.ReportGenerator;
import org.example.repository.TestExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private ReportGenerator reportGenerator;

    @Autowired
    private TestExecutionRepository testExecutionRepository;

    public String generateComprehensiveReport(List<Long> executionIds, String reportFormat, String reportType) throws IOException {
        List<TestExecution> executions = testExecutionRepository.findAllById(executionIds);
        
        switch (reportFormat.toLowerCase()) {
            case "html":
                return reportGenerator.generateHTMLReport(executions, reportType);
            case "csv":
                return reportGenerator.generateCSVReport(executions, reportType);
            case "xml":
                return reportGenerator.generateXMLReport(executions, reportType);
            case "allure":
                return reportGenerator.generateAllureReport(executions, reportType);
            default:
                throw new IllegalArgumentException("Unsupported report format: " + reportFormat);
        }
    }

    public String generateLatestExecutionsReport(String reportFormat, int limit) throws IOException {
        List<TestExecution> latestExecutions = testExecutionRepository.findTop10ByOrderByStartTimeDesc();
        
        if (limit > 0 && latestExecutions.size() > limit) {
            latestExecutions = latestExecutions.subList(0, limit);
        }

        return generateComprehensiveReport(
            latestExecutions.stream().map(TestExecution::getId).toList(), 
            reportFormat, 
            "Latest"
        );
    }

    public String generateFailedTestsReport(String reportFormat) throws IOException {
        List<TestExecution> failedExecutions = testExecutionRepository.findByStatus(TestExecution.ExecutionStatus.FAILED);
        
        return generateComprehensiveReport(
            failedExecutions.stream().map(TestExecution::getId).toList(), 
            reportFormat, 
            "FailedTests"
        );
    }

    public String generateEnvironmentReport(String environment, String reportFormat) throws IOException {
        List<TestExecution> environmentExecutions = testExecutionRepository.findByEnvironment(environment);
        
        return generateComprehensiveReport(
            environmentExecutions.stream().map(TestExecution::getId).toList(), 
            reportFormat, 
            "Environment_" + environment
        );
    }
}
