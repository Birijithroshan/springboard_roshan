package org.automation.ui;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;

public class BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        WebDriver webDriver = new ChromeDriver(options);
        DriverManager.setDriver(webDriver); // Save driver in ThreadLocal
    }

    public WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            driver.quit();
            DriverManager.removeDriver();
        }
    }

    // Screenshot helper (optional, still usable in tests)
    public void takeScreenshot(String name) {
        WebDriver driver = DriverManager.getDriver();
        if (driver == null) return;
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File destFile = new File("target/screenshots/" + name + ".png");
        destFile.getParentFile().mkdirs();
        try {
            FileUtils.copyFile(srcFile, destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
