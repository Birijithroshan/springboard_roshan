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
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String path = "target/screenshots/" + testName + "_" + timestamp + ".png";
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.createDirectories(Paths.get("target/screenshots/"));
            Files.copy(src.toPath(), Paths.get(path));
            return path;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
