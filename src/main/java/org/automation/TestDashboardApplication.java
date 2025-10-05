package org.automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for the Test Dashboard
 */
@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "org.automation.analytics.model")
@EnableJpaRepositories(basePackages = "org.automation.analytics.repo")
public class TestDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestDashboardApplication.class, args);
    }
}
