package org.example.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ScreenshotUtils {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = "test-reports/screenshots/";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public String captureScreenshot(WebDriver driver, String executionId) {
        try {
            // Create screenshots directory if it doesn't exist
            Path screenshotPath = Paths.get(SCREENSHOT_DIR);
            if (!Files.exists(screenshotPath)) {
                Files.createDirectories(screenshotPath);
            }

            // Generate filename
            String timestamp = LocalDateTime.now().format(DATE_FORMAT);
            String filename = String.format("screenshot_%s_%s.png", executionId, timestamp);
            String fullPath = SCREENSHOT_DIR + filename;

            // Capture screenshot
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            File destFile = new File(fullPath);

            // Copy file
            Files.copy(sourceFile.toPath(), destFile.toPath());

            log.info("Screenshot captured: {}", fullPath);
            return fullPath;

        } catch (IOException e) {
            log.error("Failed to capture screenshot: {}", e.getMessage());
            return null;
        }
    }

    public boolean deleteScreenshot(String screenshotPath) {
        try {
            if (screenshotPath != null && !screenshotPath.isEmpty()) {
                File file = new File(screenshotPath);
                if (file.exists()) {
                    return file.delete();
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to delete screenshot: {}", e.getMessage());
            return false;
        }
    }
}
