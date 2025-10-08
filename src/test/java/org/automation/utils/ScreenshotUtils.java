package org.automation.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotUtils {

    public static String capture(WebDriver driver, String testName) {
        if (driver == null) return null;

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String folderPath = "target/screenshots/";
            Files.createDirectories(Paths.get(folderPath));

            String path = folderPath + testName + "_FAILED_" + timestamp + ".png";
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), Paths.get(path));

            System.out.println("📸 Screenshot captured for failed test: " + path);
            return path;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
