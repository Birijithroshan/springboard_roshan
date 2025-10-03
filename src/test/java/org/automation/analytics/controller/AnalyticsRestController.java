//package org.automation.analytics.controller;
//
//
//import org.automation.analytics.model.ExecutionLog;
//import org.automation.analytics.service.AnalyticsService;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/analytics")
//public class AnalyticsRestController {
//
//    private final AnalyticsService svc;
//
//    public AnalyticsRestController(AnalyticsService svc) {
//        this.svc = svc;
//    }
//
//    @GetMapping("/summary")
//    public ResponseEntity<Map<String,Object>> summary(
//            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
//            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
//        return ResponseEntity.ok(svc.getSummary(from, to));
//    }
//
//    @GetMapping("/trends")
//    public ResponseEntity<List<Map<String,Object>>> trends(
//            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
//            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
//        return ResponseEntity.ok(svc.getTrends(from, to));
//    }
//
//    @GetMapping("/results/{suiteId}")
//    public ResponseEntity<List<ExecutionLog>> results(@PathVariable String suiteId) {
//        return ResponseEntity.ok(svc.getResultsBySuite(suiteId));
//    }
//}
