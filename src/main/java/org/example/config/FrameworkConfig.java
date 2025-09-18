package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "automation.framework")
@Data
public class FrameworkConfig {

    private WebDriver webDriver = new WebDriver();
    private Api api = new Api();
    private Database database = new Database();
    private Reporting reporting = new Reporting();
    private Execution execution = new Execution();

    @Data
    public static class WebDriver {
        private String defaultBrowser = "chrome";
        private boolean headless = false;
        private int implicitWait = 10;
        private int pageLoadTimeout = 30;
        private String downloadPath = "downloads/";
        private boolean enableScreenshots = true;
    }

    @Data
    public static class Api {
        private int connectionTimeout = 30000;
        private int socketTimeout = 60000;
        private boolean enableLogging = true;
        private String baseUri = "";
    }

    @Data
    public static class Database {
        private String url = "jdbc:h2:mem:testdb";
        private String username = "sa";
        private String password = "";
        private String driverClassName = "org.h2.Driver";
    }

    @Data
    public static class Reporting {
        private String outputPath = "test-reports/";
        private boolean generateHtml = true;
        private boolean generateCsv = true;
        private boolean generateJunit = true;
        private boolean captureScreenshots = true;
        private boolean captureLogs = true;
    }

    @Data
    public static class Execution {
        private int maxParallelThreads = 5;
        private int defaultTimeout = 300; // seconds
        private boolean enableRetry = true;
        private int maxRetryAttempts = 2;
        private boolean skipFailedTests = false;
    }
}
