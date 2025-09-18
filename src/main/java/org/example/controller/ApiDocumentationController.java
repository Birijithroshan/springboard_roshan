package org.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.Data;

import java.util.*;

@RestController
@RequestMapping("/api/docs")
@CrossOrigin(origins = "*")
public class ApiDocumentationController {

    @GetMapping("/endpoints")
    public ResponseEntity<ApiDocumentation> getAllEndpoints() {
        ApiDocumentation documentation = new ApiDocumentation();
        documentation.setTitle("Test Automation Framework API Documentation");
        documentation.setVersion("1.0");
        documentation.setDescription("Complete API documentation for the test automation framework");

        List<EndpointInfo> endpoints = new ArrayList<>();

        // Test Execution Endpoints
        endpoints.add(new EndpointInfo(
            "POST", "/api/execution/batch", "Execute Test Batch",
            "Start execution of a test batch with specified test suite and environment",
            Map.of(
                "testSuite", "string - Name of the test suite to execute",
                "environment", "string - Target environment (dev, staging, prod)",
                "parallelThreads", "integer - Number of parallel threads (default: 1)"
            ),
            "BatchResponse with batchId, status, and message"
        ));

        endpoints.add(new EndpointInfo(
            "POST", "/api/execution/single/{testCaseId}", "Execute Single Test",
            "Execute a single test case by ID",
            Map.of(
                "testCaseId", "long - ID of the test case to execute (path parameter)",
                "environment", "string - Target environment (query parameter)"
            ),
            "TestExecution object with execution details"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/execution/batch/{batchId}", "Get Batch Status",
            "Get the status and details of a specific test batch",
            Map.of("batchId", "string - Unique identifier of the batch (path parameter)"),
            "TestBatch object with batch details and status"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/execution/batch/{batchId}/executions", "Get Batch Executions",
            "Get all test executions for a specific batch",
            Map.of("batchId", "string - Unique identifier of the batch (path parameter)"),
            "List of TestExecution objects"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/execution/batches", "Get All Batches",
            "Retrieve all test batches ordered by creation date (newest first)",
            Map.of(),
            "List of TestBatch objects ordered by creation date"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/execution/batches/recent", "Get Recent Batches",
            "Retrieve recent test batches with optional limit and time filter",
            Map.of(
                "limit", "integer - Maximum number of batches to return (default: 10, max: 100)",
                "hours", "integer - Number of hours to look back (default: 24)"
            ),
            "List of recent TestBatch objects"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/execution/batches/status/{status}", "Get Batches by Status",
            "Retrieve test batches filtered by status",
            Map.of("status", "string - Batch status (SCHEDULED, RUNNING, COMPLETED, CANCELLED, FAILED)"),
            "List of TestBatch objects with specified status"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/execution/batches/active", "Get Active Batches",
            "Retrieve currently active test batches (SCHEDULED or RUNNING)",
            Map.of(),
            "List of active TestBatch objects"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/execution/executions", "Get All Executions",
            "Retrieve all test executions across all batches",
            Map.of(),
            "List of TestExecution objects"
        ));

        // Test Case Management Endpoints
        endpoints.add(new EndpointInfo(
            "GET", "/api/testcases", "Get All Test Cases",
            "Retrieve all active test cases",
            Map.of(),
            "List of TestCase objects"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/testcases/{id}", "Get Test Case",
            "Get a specific test case by ID",
            Map.of("id", "long - ID of the test case (path parameter)"),
            "TestCase object"
        ));

        endpoints.add(new EndpointInfo(
            "POST", "/api/testcases", "Create Test Case",
            "Create a new test case",
            Map.of(
                "name", "string - Name of the test case",
                "description", "string - Description of the test case",
                "testType", "enum - Type of test (WEB_UI, API)",
                "testData", "string - Test data in JSON format",
                "expectedResult", "string - Expected test result",
                "priority", "enum - Priority level (HIGH, MEDIUM, LOW)",
                "testSuite", "string - Test suite name",
                "environment", "string - Target environment"
            ),
            "Created TestCase object"
        ));

        endpoints.add(new EndpointInfo(
            "PUT", "/api/testcases/{id}", "Update Test Case",
            "Update an existing test case",
            Map.of(
                "id", "long - ID of the test case to update (path parameter)",
                "name", "string - Updated name",
                "description", "string - Updated description",
                "testType", "enum - Updated test type",
                "testData", "string - Updated test data",
                "expectedResult", "string - Updated expected result",
                "priority", "enum - Updated priority",
                "testSuite", "string - Updated test suite",
                "environment", "string - Updated environment"
            ),
            "Updated TestCase object"
        ));

        endpoints.add(new EndpointInfo(
            "DELETE", "/api/testcases/{id}", "Delete Test Case",
            "Soft delete a test case (marks as inactive)",
            Map.of("id", "long - ID of the test case to delete (path parameter)"),
            "No content (204)"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/testcases/suite/{testSuite}", "Get Test Cases by Suite",
            "Get all active test cases for a specific test suite",
            Map.of("testSuite", "string - Name of the test suite (path parameter)"),
            "List of TestCase objects"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/testcases/environment/{environment}", "Get Test Cases by Environment",
            "Get all active test cases for a specific environment",
            Map.of("environment", "string - Environment name (path parameter)"),
            "List of TestCase objects"
        ));

        // Schedule Management Endpoints
        endpoints.add(new EndpointInfo(
            "GET", "/api/schedules", "Get All Schedules",
            "Retrieve all test schedules",
            Map.of(),
            "List of TestSchedule objects"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/schedules/active", "Get Active Schedules",
            "Retrieve only active test schedules",
            Map.of(),
            "List of active TestSchedule objects"
        ));

        endpoints.add(new EndpointInfo(
            "POST", "/api/schedules", "Create Schedule",
            "Create a new test schedule",
            Map.of(
                "scheduleName", "string - Name of the schedule",
                "cronExpression", "string - Cron expression for scheduling",
                "testSuite", "string - Test suite to execute",
                "environment", "string - Target environment",
                "parallelThreads", "integer - Number of parallel threads (default: 1)"
            ),
            "Created TestSchedule object"
        ));

        endpoints.add(new EndpointInfo(
            "PUT", "/api/schedules/{id}", "Update Schedule",
            "Update an existing test schedule",
            Map.of(
                "id", "long - ID of the schedule to update (path parameter)",
                "scheduleName", "string - Updated schedule name",
                "cronExpression", "string - Updated cron expression",
                "testSuite", "string - Updated test suite",
                "environment", "string - Updated environment",
                "parallelThreads", "integer - Updated parallel threads"
            ),
            "Updated TestSchedule object"
        ));

        endpoints.add(new EndpointInfo(
            "DELETE", "/api/schedules/{id}", "Delete Schedule",
            "Soft delete a test schedule (marks as inactive)",
            Map.of("id", "long - ID of the schedule to delete (path parameter)"),
            "No content (204)"
        ));

        endpoints.add(new EndpointInfo(
            "POST", "/api/schedules/{id}/activate", "Activate Schedule",
            "Activate a test schedule",
            Map.of("id", "long - ID of the schedule to activate (path parameter)"),
            "Activated TestSchedule object"
        ));

        endpoints.add(new EndpointInfo(
            "POST", "/api/schedules/{id}/deactivate", "Deactivate Schedule",
            "Deactivate a test schedule",
            Map.of("id", "long - ID of the schedule to deactivate (path parameter)"),
            "Deactivated TestSchedule object"
        ));

        // Reporting Endpoints
        endpoints.add(new EndpointInfo(
            "POST", "/api/reports/generate/{batchId}", "Generate All Reports",
            "Generate all types of reports for a specific batch",
            Map.of("batchId", "string - Unique identifier of the batch (path parameter)"),
            "String message confirming report generation started"
        ));

        endpoints.add(new EndpointInfo(
            "POST", "/api/reports/html/{batchId}", "Generate HTML Report",
            "Generate HTML report for a specific batch",
            Map.of("batchId", "string - Unique identifier of the batch (path parameter)"),
            "ReportResult object with report details"
        ));

        // Analytics Endpoints
        endpoints.add(new EndpointInfo(
            "GET", "/api/analytics/trends", "Get Trend Analysis",
            "Get test trend analysis for a specified date range",
            Map.of(
                "fromDate", "datetime - Start date for analysis (query parameter, ISO format)",
                "toDate", "datetime - End date for analysis (query parameter, ISO format)"
            ),
            "TestTrendAnalysis object with trend data"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/analytics/regression/{environment}", "Get Regression Metrics",
            "Get regression metrics for a specific environment",
            Map.of(
                "environment", "string - Environment name (path parameter)",
                "days", "integer - Number of days to analyze (query parameter, default: 30)"
            ),
            "RegressionMetrics object with regression data"
        ));

        // Documentation Endpoint
        endpoints.add(new EndpointInfo(
            "GET", "/api/docs/endpoints", "Get API Documentation",
            "Get complete API documentation with all available endpoints",
            Map.of(),
            "ApiDocumentation object with all endpoint information"
        ));

        // Demo Data Endpoints
        endpoints.add(new EndpointInfo(
            "GET", "/api/demo/test-cases", "Get Demo Test Cases",
            "Retrieve sample test cases for demonstration purposes",
            Map.of(),
            "List of sample TestCase objects with realistic data"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/demo/executions", "Get Demo Executions",
            "Retrieve sample test executions for demonstration purposes",
            Map.of(),
            "List of sample execution objects with status and timing data"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/demo/batches", "Get Demo Batches",
            "Retrieve sample test batches for demonstration purposes",
            Map.of(),
            "List of sample batch objects with various statuses"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/demo/sample-data", "Get All Sample Data",
            "Retrieve comprehensive sample data including test cases, executions, batches, metrics, and environments",
            Map.of(),
            "Comprehensive object containing all sample data for demo purposes"
        ));

        endpoints.add(new EndpointInfo(
            "POST", "/api/demo/create-sample-test", "Create Sample Test",
            "Create a sample test case for demonstration purposes",
            Map.of("testData", "object - Test case data in JSON format"),
            "Response object with success status, test ID, and confirmation message"
        ));

        // Dashboard Endpoints
        endpoints.add(new EndpointInfo(
            "GET", "/api/dashboard/metrics", "Get Dashboard Metrics",
            "Retrieve comprehensive dashboard metrics including test cases, batches, executions, and schedules",
            Map.of(),
            "Dashboard metrics object with counts, success rates, and status breakdowns"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/dashboard/recent-activity", "Get Recent Activity",
            "Retrieve recent activity including recent batches and executions from the last 24 hours",
            Map.of(),
            "Recent activity object with recent batches, execution counts, and timestamp information"
        ));

        // Validation Endpoints
        endpoints.add(new EndpointInfo(
            "GET", "/api/validation/framework", "Validate Framework",
            "Perform comprehensive framework validation with real data to ensure system integrity",
            Map.of(),
            "ValidationResult object with validation status, checks performed, and any issues found"
        ));

        endpoints.add(new EndpointInfo(
            "GET", "/api/validation/health", "Health Check",
            "Perform a basic health check of the validation service",
            Map.of(),
            "String message confirming validation service status"
        ));

        documentation.setEndpoints(endpoints);
        documentation.setTotalEndpoints(endpoints.size());

        return ResponseEntity.ok(documentation);
    }

    @Data
    public static class ApiDocumentation {
        private String title;
        private String version;
        private String description;
        private int totalEndpoints;
        private List<EndpointInfo> endpoints;
    }

    @Data
    public static class EndpointInfo {
        private String method;
        private String path;
        private String name;
        private String description;
        private Map<String, String> parameters;
        private String response;

        public EndpointInfo(String method, String path, String name, String description,
                           Map<String, String> parameters, String response) {
            this.method = method;
            this.path = path;
            this.name = name;
            this.description = description;
            this.parameters = parameters;
            this.response = response;
        }
    }
}
