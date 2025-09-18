package org.example.controller;

import org.example.analytics.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/trends")
    public ResponseEntity<AnalyticsService.TestTrendAnalysis> getTrendAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        AnalyticsService.TestTrendAnalysis analysis = analyticsService.generateTrendAnalysis(fromDate, toDate);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/regression/{environment}")
    public ResponseEntity<AnalyticsService.RegressionMetrics> getRegressionMetrics(
            @PathVariable String environment,
            @RequestParam(defaultValue = "30") int days) {

        AnalyticsService.RegressionMetrics metrics = analyticsService.calculateRegressionMetrics(environment, days);
        return ResponseEntity.ok(metrics);
    }
}
