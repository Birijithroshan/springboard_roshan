package org.example.engine;

import static io.github.bonigarcia.wdm.WebDriverManager.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebDriverManager {

    private static final Logger log = LoggerFactory.getLogger(WebDriverManager.class);
    private final ConcurrentHashMap<String, WebDriver> driverInstances = new ConcurrentHashMap<>();

    public WebDriver createDriver(String browserType, boolean headless) {
        String threadId = Thread.currentThread().getName();
        WebDriver driver = null;

        try {
            switch (browserType.toLowerCase()) {
                case "chrome":
                    chromedriver().setup();
                    ChromeOptions chromeOptions = new ChromeOptions();
                    if (headless) {
                        chromeOptions.addArguments("--headless");
                    }
                    chromeOptions.addArguments("--no-sandbox");
                    chromeOptions.addArguments("--disable-dev-shm-usage");
                    chromeOptions.addArguments("--disable-gpu");
                    chromeOptions.setBinary("/usr/bin/chromium-browser");
                    driver = new ChromeDriver(chromeOptions);
                    break;

                case "firefox":
                    firefoxdriver().setup();
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    if (headless) {
                        firefoxOptions.addArguments("--headless");
                    }
                    driver = new FirefoxDriver(firefoxOptions);
                    break;

                case "edge":
                    edgedriver().setup();
                    EdgeOptions edgeOptions = new EdgeOptions();
                    if (headless) {
                        edgeOptions.addArguments("--headless");
                    }
                    driver = new EdgeDriver(edgeOptions);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported browser: " + browserType);
            }

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().window().maximize();

            driverInstances.put(threadId, driver);
            log.info("Created {} driver for thread {}", browserType, threadId);

        } catch (Exception e) {
            log.error("Failed to create {} driver: {}", browserType, e.getMessage());
            throw new RuntimeException("Driver creation failed", e);
        }

        return driver;
    }

    public WebDriver getDriver() {
        String threadId = Thread.currentThread().getName();
        return driverInstances.get(threadId);
    }

    public void quitDriver() {
        String threadId = Thread.currentThread().getName();
        WebDriver driver = driverInstances.get(threadId);
        if (driver != null) {
            try {
                driver.quit();
                driverInstances.remove(threadId);
                log.info("Quit driver for thread {}", threadId);
            } catch (Exception e) {
                log.error("Error quitting driver: {}", e.getMessage());
            }
        }
    }

    public void quitAllDrivers() {
        driverInstances.forEach((threadId, driver) -> {
            try {
                driver.quit();
                log.info("Quit driver for thread {}", threadId);
            } catch (Exception e) {
                log.error("Error quitting driver for thread {}: {}", threadId, e.getMessage());
            }
        });
        driverInstances.clear();
    }
}
