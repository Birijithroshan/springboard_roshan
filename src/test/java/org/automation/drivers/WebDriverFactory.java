package org.automation.drivers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverFactory {
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static void initDriver() {
        if (driverThreadLocal.get() == null) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            driverThreadLocal.set(new ChromeDriver(options));
        }
    }

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void quitDriver() {
        if (driverThreadLocal.get() != null) {
            driverThreadLocal.get().quit();
            driverThreadLocal.remove();
        }
    }
}
