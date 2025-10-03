//package org.automation.analytics.service;
//
//import org.automation.analytics.model.ExecutionLog;
//import org.automation.analytics.repo.ExecutionLogRepository;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class AnalyticsService {
//
//    private final ExecutionLogRepository repo;
//
//    public AnalyticsService(ExecutionLogRepository repo) {
//        this.repo = repo;
//    }
//
//    public Map<String, Object> getSummary(LocalDate from, LocalDate to) {
//        LocalDateTime f = (from != null) ? from.atStartOfDay() : LocalDate.now().minusDays(7).atStartOfDay();
//        LocalDateTime t = (to != null) ? to.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);
//
//        List<ExecutionLog> rows = repo.findAll().stream()
//                .filter(r -> r.getStartTime() != null && !r.getStartTime().isBefore(f) && !r.getStartTime().isAfter(t))
//                .collect(Collectors.toList());
//
//        long total = rows.size();
//        long passed = rows.stream().filter(r -> "PASS".equalsIgnoreCase(r.getStatus())).count();
//        long failed = rows.stream().filter(r -> "FAIL".equalsIgnoreCase(r.getStatus())).count();
//        double passRate = total == 0 ? 0.0 : (passed * 100.0 / total);
//
//        // ✅ FIX: no null check — durationMs is a primitive long (0 if unset)
//        double avgDuration = total == 0 ? 0.0 : rows.stream().mapToLong(ExecutionLog::getDurationMs).average().orElse(0);
//
//        Map<String, Object> out = new HashMap<>();
//        out.put("total", total);
//        out.put("passed", passed);
//        out.put("failed", failed);
//        out.put("passRate", passRate);
//        out.put("avgDurationMs", avgDuration);
//        return out;
//    }
//
//    public List<Map<String, Object>> getTrends(LocalDate from, LocalDate to) {
//        LocalDate start = (from != null) ? from : LocalDate.now().minusDays(7);
//        LocalDate end = (to != null) ? to : LocalDate.now();
//
//        LocalDateTime f = start.atStartOfDay();
//        LocalDateTime t = end.atTime(LocalTime.MAX);
//
//        List<ExecutionLog> rows = repo.findAll().stream()
//                .filter(r -> r.getStartTime() != null && !r.getStartTime().isBefore(f) && !r.getStartTime().isAfter(t))
//                .collect(Collectors.toList());
//
//        Map<LocalDate, long[]> map = new TreeMap<>();
//        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
//            map.put(d, new long[]{0, 0}); // [passed, failed]
//        }
//
//        for (ExecutionLog r : rows) {
//            LocalDate d = r.getStartTime().toLocalDate();
//            long[] counts = map.getOrDefault(d, new long[]{0, 0});
//            if ("PASS".equalsIgnoreCase(r.getStatus())) counts[0]++;
//            else if ("FAIL".equalsIgnoreCase(r.getStatus())) counts[1]++;
//            map.put(d, counts);
//        }
//
//        List<Map<String, Object>> out = new ArrayList<>();
//        map.forEach((date, counts) -> {
//            Map<String, Object> m = new HashMap<>();
//            m.put("date", date.toString());
//            m.put("passed", counts[0]);
//            m.put("failed", counts[1]);
//            out.add(m);
//        });
//        return out;
//    }
//
//    public List<ExecutionLog> getResultsBySuite(String suiteId) {
//        return repo.findBySuiteId(suiteId);
//    }
//}
//
