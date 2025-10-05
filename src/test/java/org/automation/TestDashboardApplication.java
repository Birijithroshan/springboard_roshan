package org.automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for the Test Dashboard
 */
@SpringBootApplication
@EnableScheduling
public class TestDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestDashboardApplication.class, args);
    }
}
