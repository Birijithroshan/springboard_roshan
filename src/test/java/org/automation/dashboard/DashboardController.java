package org.automation.dashboard;

import org.automation.analytics.service.AnalyticsService;
import org.automation.analytics.model.ExecutionLog;
import org.automation.utils.DatabaseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main controller for the Test Dashboard web interface
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        try {
            // Get test execution statistics
            Map<String, Object> stats = analyticsService.getTestExecutionStats();
            model.addAttribute("stats", stats);

            // Get recent test executions
            List<ExecutionLog> recentExecutions = analyticsService.getRecentExecutions(10);
            model.addAttribute("recentExecutions", recentExecutions);

            // Get last updated timestamp
            String lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            model.addAttribute("lastUpdated", lastUpdated);

            return "dashboard/index";
        } catch (Exception e) {
            logger.error("Error loading dashboard: {}", e.getMessage());
            model.addAttribute("error", "Failed to load dashboard data");
            return "error";
        }
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        try {
            // Get available test reports
            List<String> htmlReports = getAvailableReports("html");
            List<String> xmlReports = getAvailableReports("xml");
            List<String> jsonReports = getAvailableReports("json");

            model.addAttribute("htmlReports", htmlReports);
            model.addAttribute("xmlReports", xmlReports);
            model.addAttribute("jsonReports", jsonReports);

            return "dashboard/reports";
        } catch (Exception e) {
            logger.error("Error loading reports: {}", e.getMessage());
            model.addAttribute("error", "Failed to load reports");
            return "error";
        }
    }

    @GetMapping("/test-manager")
    public String testManager(Model model) {
        try {
            // Get available test classes
            List<String> uiTests = getAvailableTestClasses("ui");
            List<String> apiTests = getAvailableTestClasses("api");

            model.addAttribute("uiTests", uiTests);
            model.addAttribute("apiTests", apiTests);

            return "dashboard/test-manager";
        } catch (Exception e) {
            logger.error("Error loading test manager: {}", e.getMessage());
            model.addAttribute("error", "Failed to load test manager");
            return "error";
        }
    }

    @PostMapping("/run-tests")
    @ResponseBody
    public ResponseEntity<Map<String, String>> runTests(@RequestBody Map<String, Object> request) {
        Map<String, String> response = new HashMap<>();

        try {
            String suite = (String) request.get("suite");
            String browser = (String) request.getOrDefault("browser", "chrome");
            boolean headless = (boolean) request.getOrDefault("headless", false);

            // Execute tests asynchronously
            String executionId = analyticsService.executeTestSuite(suite, browser, headless);

            response.put("status", "success");
            response.put("message", "Test execution started");
            response.put("executionId", executionId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error starting test execution: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "Failed to start test execution: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/execution-status/{executionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getExecutionStatus(@PathVariable String executionId) {
        try {
            Map<String, Object> status = analyticsService.getExecutionStatus(executionId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Error getting execution status: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/upload-test")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadTest(@RequestParam("file") MultipartFile file,
                                                          @RequestParam("testType") String testType) {
        Map<String, String> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Save the uploaded test file
            String fileName = file.getOriginalFilename();
            String targetDir = testType.equals("ui") ? "src/test/java/org/automation/ui/" : "src/test/java/org/automation/api/";
            Path targetPath = Paths.get(targetDir + fileName);

            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, file.getBytes());

            response.put("status", "success");
            response.put("message", "Test file uploaded successfully");
            response.put("fileName", fileName);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Error uploading test file: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "Failed to upload test file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApiStats() {
        try {
            Map<String, Object> stats = analyticsService.getTestExecutionStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting API stats: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private List<String> getAvailableReports(String type) {
        try {
            Path reportsDir = Paths.get("artifacts/reports");
            if (Files.exists(reportsDir)) {
                return Files.walk(reportsDir)
                    .filter(path -> path.toString().endsWith("." + type))
                    .map(path -> path.getFileName().toString())
                    .toList();
            }
        } catch (IOException e) {
            logger.warn("Error reading reports directory: {}", e.getMessage());
        }
        return List.of();
    }

    private List<String> getAvailableTestClasses(String type) {
        try {
            Path testDir = Paths.get("src/test/java/org/automation/" + type);
            if (Files.exists(testDir)) {
                return Files.walk(testDir)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> path.getFileName().toString().replace(".java", ""))
                    .toList();
            }
        } catch (IOException e) {
            logger.warn("Error reading test directory: {}", e.getMessage());
        }
        return List.of();
    }
}
