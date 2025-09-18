package org.example.engine;

import org.example.model.TestCase;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class WebUITestExecutor {

    private static final Logger log = LoggerFactory.getLogger(WebUITestExecutor.class);

    @Value("${automation.framework.webDriver.defaultBrowser:chrome}")
    private String defaultBrowser;

    @Value("${automation.framework.webDriver.headless:false}")
    private boolean headlessMode;

    @Value("${automation.framework.webDriver.implicitWait:10}")
    private int implicitWait;

    @Value("${automation.framework.reporting.outputPath:test-reports/}")
    private String reportOutputPath;

    @Value("${automation.framework.reporting.captureScreenshots:true}")
    private boolean captureScreenshots;

    @Value("${automation.framework.webDriver.chromiumPath:/usr/bin/chromium-browser}")
    private String chromiumPath;

    @Value("${automation.framework.webDriver.autoDetectDisplay:true}")
    private boolean autoDetectDisplay; // now used in environment detection

    private WebDriver webDriver;
    private WebDriverWait driverWait;

    public TestExecutionEngine.TestExecutionResult execute(Map<String, Object> testData, TestCase testCase) {
        TestExecutionEngine.TestExecutionResult result = new TestExecutionEngine.TestExecutionResult();
        List<String> logs = new ArrayList<>();
        List<String> screenshots = new ArrayList<>();

        try {
            logs.add("Starting Web UI test execution for: " + testCase.getName());
            
            // Initialize WebDriver
            initializeDriver(getBrowserFromTestData(testData, defaultBrowser));
            logs.add("WebDriver initialized: " + defaultBrowser);

            // Execute test based on data structure
            if (testData.containsKey("steps")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> steps = (List<Map<String, Object>>) testData.get("steps");
                result = executeSteps(steps, logs, screenshots);
            } else {
                result = executeSingleAction(testData, logs, screenshots);
            }

            if (result.isSuccess()) {
                logs.add("Web UI test completed successfully");
            }

            result.setExecutionLogs(String.join("\n", logs));

        } catch (Exception e) {
            log.error("Web UI test execution failed", e);
            result.setSuccess(false);
            result.setErrorMessage("Web UI execution failed: " + e.getMessage());
            
            // Capture screenshot on error
            if (captureScreenshots && webDriver != null) {
                String screenshotPath = captureScreenshot("error_" + System.currentTimeMillis());
                if (screenshotPath != null) {
                    screenshots.add(screenshotPath);
                }
            }

            logs.add("ERROR: " + e.getMessage());
            result.setExecutionLogs(String.join("\n", logs));

        } finally {
            result.setScreenshotPaths(screenshots);
            cleanupDriver();
        }

        return result;
    }

    private TestExecutionEngine.TestExecutionResult executeSteps(List<Map<String, Object>> steps, 
                                                               List<String> logs, List<String> screenshots) {
        TestExecutionEngine.TestExecutionResult result = new TestExecutionEngine.TestExecutionResult();
        result.setSuccess(true);

        for (int i = 0; i < steps.size(); i++) {
            Map<String, Object> step = steps.get(i);
            String action = step.getOrDefault("action", "").toString();
            
            logs.add(String.format("Executing step %d: %s", i + 1, action));

            try {
                switch (action.toLowerCase()) {
                    case "navigate":
                        navigateTo(step.get("url").toString());
                        break;
                    case "click":
                        clickElement(step.get("selector").toString());
                        break;
                    case "type":
                    case "input":
                        typeText(step.get("selector").toString(), step.get("text").toString());
                        break;
                    case "wait":
                        waitForElement(step.get("selector").toString());
                        break;
                    case "verify":
                    case "assert":
                        boolean verified = verifyElement(step.get("selector").toString(), 
                                                       step.getOrDefault("expectedText", "").toString());
                        if (!verified) {
                            result.setSuccess(false);
                            result.setErrorMessage("Verification failed at step " + (i + 1));
                            
                            if (captureScreenshots) {
                                String screenshotPath = captureScreenshot("verification_failed_step_" + (i + 1));
                                if (screenshotPath != null) {
                                    screenshots.add(screenshotPath);
                                }
                            }
                            return result;
                        }
                        break;
                    case "screenshot":
                        if (captureScreenshots) {
                            String screenshotPath = captureScreenshot("step_" + (i + 1));
                            if (screenshotPath != null) {
                                screenshots.add(screenshotPath);
                            }
                        }
                        break;
                    default:
                        logs.add("Unknown action: " + action);
                }

                logs.add(String.format("Step %d completed successfully", i + 1));

            } catch (Exception e) {
                log.error("Step {} execution failed: {}", i + 1, e.getMessage());
                result.setSuccess(false);
                result.setErrorMessage("Step " + (i + 1) + " failed: " + e.getMessage());
                
                if (captureScreenshots) {
                    String screenshotPath = captureScreenshot("error_step_" + (i + 1));
                    if (screenshotPath != null) {
                        screenshots.add(screenshotPath);
                    }
                }
                
                return result;
            }
        }

        return result;
    }

    private TestExecutionEngine.TestExecutionResult executeSingleAction(Map<String, Object> testData, 
                                                                      List<String> logs, List<String> screenshots) {
        TestExecutionEngine.TestExecutionResult result = new TestExecutionEngine.TestExecutionResult();
        result.setSuccess(true);

        try {
            // Navigate to URL if provided
            if (testData.containsKey("url")) {
                navigateTo(testData.get("url").toString());
                logs.add("Navigated to: " + testData.get("url"));
            }

            // Execute basic login test as example
            if (testData.containsKey("username") && testData.containsKey("password")) {
                executeLoginTest(testData, logs);
            }

            // Take final screenshot if enabled
            if (captureScreenshots) {
                String screenshotPath = captureScreenshot("final_result");
                if (screenshotPath != null) {
                    screenshots.add(screenshotPath);
                }
            }

            logs.add("Single action test completed successfully");

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Single action test failed: " + e.getMessage());
            logs.add("ERROR: " + e.getMessage());
        }

        return result;
    }

    private void initializeDriver(String browser) {
        try {
            log.info("Initializing WebDriver for browser: {}", browser);

            // Add system property to suppress CDP warnings for Chrome 140
            System.setProperty("webdriver.chrome.silentOutput", "true");
            System.setProperty("org.openqa.selenium.logging.ignore", "org.openqa.selenium.devtools");

            ChromeOptions chromeOptions = new ChromeOptions();
            FirefoxOptions firefoxOptions = new FirefoxOptions();

            // Enhanced Chrome options for Linux environment
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--disable-dev-shm-usage");
            chromeOptions.addArguments("--disable-gpu");
            chromeOptions.addArguments("--disable-extensions");
            chromeOptions.addArguments("--disable-web-security");
            chromeOptions.addArguments("--allow-running-insecure-content");
            chromeOptions.addArguments("--disable-background-timer-throttling");
            chromeOptions.addArguments("--disable-backgrounding-occluded-windows");
            chromeOptions.addArguments("--disable-renderer-backgrounding");
            chromeOptions.addArguments("--disable-features=TranslateUI");
            chromeOptions.addArguments("--window-size=1920,1080");

            // Disable DevTools Protocol features to avoid CDP version mismatch
            chromeOptions.addArguments("--disable-dev-tools");
            chromeOptions.addArguments("--disable-extensions-http-throttling");
            chromeOptions.addArguments("--disable-logging");
            chromeOptions.addArguments("--disable-background-networking");
            chromeOptions.addArguments("--disable-default-apps");
            chromeOptions.addArguments("--disable-sync");
            chromeOptions.addArguments("--no-first-run");
            chromeOptions.addArguments("--disable-features=VizDisplayCompositor");

            // Set experimental options to disable CDP features
            chromeOptions.setExperimentalOption("useAutomationExtension", false);
            // Replace Arrays.asList single element warning with singletonList
            chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

            // Check if running in headless mode or on server environment
            if (headlessMode || isServerEnvironment()) {
                chromeOptions.addArguments("--headless=new");
                chromeOptions.addArguments("--enable-features=NetworkService,NetworkServiceLogging");
                chromeOptions.addArguments("--disable-client-side-phishing-detection");
                chromeOptions.addArguments("--disable-hang-monitor");
                chromeOptions.addArguments("--disable-popup-blocking");
                chromeOptions.addArguments("--disable-prompt-on-repost");
                chromeOptions.addArguments("--metrics-recording-only");
                chromeOptions.addArguments("--safebrowsing-disable-auto-update");
                chromeOptions.addArguments("--enable-automation");
                chromeOptions.addArguments("--password-store=basic");
                chromeOptions.addArguments("--use-mock-keychain");
                log.info("Running in headless mode");
            }

            // Firefox options for Linux
            if (headlessMode || isServerEnvironment()) {
                firefoxOptions.addArguments("--headless");
            }
            firefoxOptions.addPreference("dom.webdriver.enabled", false);
            firefoxOptions.addPreference("useAutomationExtension", false);

            switch (browser.toLowerCase()) {
                case "firefox":
                    log.info("Setting up Firefox WebDriver");
                    try {
                        WebDriverManager.firefoxdriver().setup();
                        webDriver = new FirefoxDriver(firefoxOptions);
                        log.info("Firefox WebDriver initialized successfully");
                    } catch (Exception e) {
                        log.error("Firefox WebDriver initialization failed: {}", e.getMessage());
                        // Fallback to Chrome
                        log.info("Falling back to Chrome WebDriver");
                        setupChromeDriver(chromeOptions);
                    }
                    break;
                case "chrome":
                default:
                    setupChromeDriver(chromeOptions);
                    break;
            }

            if (webDriver != null) {
                webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
                webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
                if (!headlessMode && !isServerEnvironment()) {
                    webDriver.manage().window().maximize();
                }
                driverWait = new WebDriverWait(webDriver, Duration.ofSeconds(20));
                log.info("WebDriver configured successfully with timeouts and window settings");
            } else {
                throw new RuntimeException("Failed to initialize any WebDriver");
            }

        } catch (Exception e) {
            log.error("Failed to initialize WebDriver: {}", e.getMessage(), e);
            throw new RuntimeException("WebDriver initialization failed: " + e.getMessage(), e);
        }
    }

    private void setupChromeDriver(ChromeOptions chromeOptions) {
        log.info("Setting up Chrome WebDriver");
        try {
            // Check if Chrome is available
            if (!isChromeAvailable()) {
                log.warn("Chrome browser not detected, attempting to install or use alternative");
                // Try to use Chromium as fallback
                chromeOptions.setBinary(chromiumPath);
            }

            // Configure WebDriverManager to use compatible ChromeDriver version
            // This will automatically download the correct ChromeDriver version for your Chrome browser
            WebDriverManager chromeDriverManager = WebDriverManager.chromedriver();

            // Force WebDriverManager to check for the correct version matching your Chrome installation
            chromeDriverManager.clearDriverCache(); // Clear any cached incompatible drivers
            chromeDriverManager.setup();

            log.info("WebDriverManager configured ChromeDriver for Chrome browser");

            webDriver = new ChromeDriver(chromeOptions);
            log.info("Chrome WebDriver initialized successfully");
        } catch (Exception e) {
            log.error("Chrome WebDriver initialization failed: {}", e.getMessage());
            // Try with Chromium as fallback
            try {
                log.info("Attempting to use Chromium browser as fallback");
                chromeOptions.setBinary(chromiumPath);

                // Try WebDriverManager setup again for Chromium path
                WebDriverManager.chromedriver().clearDriverCache().setup();

                webDriver = new ChromeDriver(chromeOptions);
                log.info("Chromium WebDriver initialized successfully");
            } catch (Exception chromiumError) {
                log.error("Chromium WebDriver also failed: {}", chromiumError.getMessage());

                // Final fallback: try to use a specific ChromeDriver version
                try {
                    log.info("Attempting final fallback with ChromeDriver version compatibility");
                    WebDriverManager.chromedriver()
                        .browserVersion("139") // Match your Chrome version
                        .setup();
                    webDriver = new ChromeDriver(chromeOptions);
                    log.info("ChromeDriver 139 compatibility mode initialized successfully");
                } catch (Exception finalError) {
                    log.error("All ChromeDriver initialization attempts failed: {}", finalError.getMessage());
                    throw new RuntimeException("All Chrome and Chromium WebDriver initialization attempts failed", finalError);
                }
            }
        }
    }

    private boolean isServerEnvironment() {
        // If auto detection disabled, rely purely on configured headless flag
        if (!autoDetectDisplay) {
            log.debug("Auto display detection disabled; using headlessMode config: {}", headlessMode);
            return headlessMode;
        }
        // Check if running on a server (no display)
        String display = System.getenv("DISPLAY");
        String sessionType = System.getenv("XDG_SESSION_TYPE");
        boolean isHeadless = display == null || display.trim().isEmpty();

        log.debug("Environment check - DISPLAY: {}, XDG_SESSION_TYPE: {}, isHeadless: {}",
                 display, sessionType, isHeadless);

        return isHeadless;
    }

    private boolean isChromeAvailable() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"which", "google-chrome"});
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.debug("Google Chrome found via 'which' command");
                return true;
            }

            // Try alternative Chrome locations
            String[] chromePaths = {
                "/usr/bin/google-chrome",
                "/usr/bin/google-chrome-stable",
                "/usr/bin/chromium-browser",
                "/usr/bin/chromium",
                "/snap/bin/chromium"
            };

            for (String path : chromePaths) {
                File chromeFile = new File(path);
                if (chromeFile.exists() && chromeFile.canExecute()) {
                    log.debug("Chrome/Chromium found at: {}", path);
                    return true;
                }
            }

            log.warn("Chrome browser not found in standard locations");
            return false;
        } catch (Exception e) {
            log.warn("Error checking Chrome availability: {}", e.getMessage());
            return false;
        }
    }

    private void navigateTo(String url) {
        if (webDriver != null) {
            webDriver.get(url);
            log.info("Navigated to: {}", url);
        }
    }

    private void clickElement(String selector) {
        WebElement element = driverWait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
        element.click();
        log.info("Clicked element: {}", selector);
    }

    private void typeText(String selector, String text) {
        WebElement element = driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)));
        element.clear();
        element.sendKeys(text);
        log.info("Typed text '{}' into element: {}", text, selector);
    }

    private void waitForElement(String selector) {
        driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)));
        log.info("Waited for element: {}", selector);
    }

    private boolean verifyElement(String selector, String expectedText) {
        try {
            WebElement element = driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)));
            
            if (expectedText == null || expectedText.isEmpty()) {
                return element.isDisplayed();
            } else {
                return element.getText().contains(expectedText);
            }
        } catch (Exception e) {
            log.warn("Element verification failed for selector: {}", selector);
            return false;
        }
    }

    private void executeLoginTest(Map<String, Object> testData, List<String> logs) {
        try {
            String username = testData.get("username").toString();
            String password = testData.get("password").toString();

            // Try common username selectors
            String[] usernameSelectors = {"#username", "#user", "input[name='username']", "input[type='email']"};
            String[] passwordSelectors = {"#password", "#pass", "input[name='password']", "input[type='password']"};
            String[] submitSelectors = {"input[type='submit']", "button[type='submit']", "#login", ".login-button"};

            boolean loginExecuted = false;

            for (String userSelector : usernameSelectors) {
                try {
                    WebElement userElement = webDriver.findElement(By.cssSelector(userSelector));
                    if (userElement.isDisplayed()) {
                        userElement.clear();
                        userElement.sendKeys(username);
                        
                        for (String passSelector : passwordSelectors) {
                            try {
                                WebElement passElement = webDriver.findElement(By.cssSelector(passSelector));
                                if (passElement.isDisplayed()) {
                                    passElement.clear();
                                    passElement.sendKeys(password);
                                    
                                    for (String submitSelector : submitSelectors) {
                                        try {
                                            WebElement submitElement = webDriver.findElement(By.cssSelector(submitSelector));
                                            if (submitElement.isDisplayed()) {
                                                submitElement.click();
                                                loginExecuted = true;
                                                logs.add("Login executed successfully");
                                                Thread.sleep(2000); // Wait for login to process
                                                return;
                                            }
                                        } catch (Exception ignored) {}
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (!loginExecuted) {
                logs.add("Could not find standard login elements, login not executed");
            }

        } catch (Exception e) {
            log.error("Login test execution failed", e);
            throw e;
        }
    }

    private String captureScreenshot(String name) {
        if (webDriver == null) return null;

        try {
            // Create screenshots directory
            Path screenshotDir = Paths.get(reportOutputPath, "screenshots");
            Files.createDirectories(screenshotDir);

            // Generate filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s_%s.png", name, timestamp);
            File screenshotFile = screenshotDir.resolve(filename).toFile();

            // Take screenshot
            TakesScreenshot takesScreenshot = (TakesScreenshot) webDriver;
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            Files.copy(sourceFile.toPath(), screenshotFile.toPath());

            log.info("Screenshot captured: {}", screenshotFile.getAbsolutePath());
            return screenshotFile.getAbsolutePath();

        } catch (IOException e) {
            log.error("Failed to capture screenshot: {}", e.getMessage());
            return null;
        }
    }

    private String getBrowserFromTestData(Map<String, Object> testData, String defaultBrowser) {
        return testData.getOrDefault("browser", defaultBrowser).toString();
    }

    private void cleanupDriver() {
        if (webDriver != null) {
            try {
                webDriver.quit();
                log.info("WebDriver cleaned up successfully");
            } catch (Exception e) {
                log.warn("Error during WebDriver cleanup: {}", e.getMessage());
            } finally {
                webDriver = null;
                driverWait = null;
            }
        }
    }
}
