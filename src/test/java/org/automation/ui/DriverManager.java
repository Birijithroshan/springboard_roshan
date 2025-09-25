package org.automation.ui;

import org.openqa.selenium.WebDriver;

public class DriverManager {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static void setDriver(WebDriver driver) {
        driverThreadLocal.set(driver);
    }

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void removeDriver() {
        driverThreadLocal.remove();
    }
}
