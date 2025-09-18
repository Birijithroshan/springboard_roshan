package org.example.controller;

import org.example.model.TestSchedule;
import org.example.repository.TestScheduleRepository;
import org.example.scheduler.TestScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private TestScheduleRepository scheduleRepository;

    @Autowired
    private TestScheduler testScheduler;

    @GetMapping
    public ResponseEntity<List<TestSchedule>> getAllSchedules() {
        List<TestSchedule> schedules = scheduleRepository.findAll();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/active")
    public ResponseEntity<List<TestSchedule>> getActiveSchedules() {
        List<TestSchedule> schedules = scheduleRepository.findByIsActiveTrue();
        return ResponseEntity.ok(schedules);
    }

    @PostMapping
    public ResponseEntity<TestSchedule> createSchedule(@RequestBody ScheduleRequest request) {
        TestSchedule schedule = new TestSchedule();
        schedule.setScheduleName(request.getScheduleName());
        schedule.setCronExpression(request.getCronExpression());
        schedule.setTestSuite(request.getTestSuite());
        schedule.setEnvironment(request.getEnvironment());
        schedule.setParallelThreads(request.getParallelThreads());
        schedule.setIsActive(true);

        TestSchedule savedSchedule = scheduleRepository.save(schedule);

        // Schedule the job
        testScheduler.scheduleTest(savedSchedule);

        return ResponseEntity.ok(savedSchedule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestSchedule> updateSchedule(@PathVariable Long id, @RequestBody ScheduleRequest request) {
        Optional<TestSchedule> optionalSchedule = scheduleRepository.findById(id);
        if (optionalSchedule.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TestSchedule schedule = optionalSchedule.get();
        schedule.setScheduleName(request.getScheduleName());
        schedule.setCronExpression(request.getCronExpression());
        schedule.setTestSuite(request.getTestSuite());
        schedule.setEnvironment(request.getEnvironment());
        schedule.setParallelThreads(request.getParallelThreads());

        TestSchedule updatedSchedule = scheduleRepository.save(schedule);

        // Reschedule the job
        testScheduler.rescheduleTest(updatedSchedule);

        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        Optional<TestSchedule> schedule = scheduleRepository.findById(id);
        if (schedule.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Unschedule the job
        testScheduler.unscheduleTest(id);

        TestSchedule s = schedule.get();
        s.setIsActive(false);
        scheduleRepository.save(s);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<TestSchedule> activateSchedule(@PathVariable Long id) {
        Optional<TestSchedule> optionalSchedule = scheduleRepository.findById(id);
        if (optionalSchedule.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TestSchedule schedule = optionalSchedule.get();
        schedule.setIsActive(true);
        TestSchedule updatedSchedule = scheduleRepository.save(schedule);

        testScheduler.scheduleTest(updatedSchedule);

        return ResponseEntity.ok(updatedSchedule);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<TestSchedule> deactivateSchedule(@PathVariable Long id) {
        Optional<TestSchedule> optionalSchedule = scheduleRepository.findById(id);
        if (optionalSchedule.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TestSchedule schedule = optionalSchedule.get();
        schedule.setIsActive(false);
        TestSchedule updatedSchedule = scheduleRepository.save(schedule);

        testScheduler.unscheduleTest(id);

        return ResponseEntity.ok(updatedSchedule);
    }

    @Data
    public static class ScheduleRequest {
        private String scheduleName;
        private String cronExpression;
        private String testSuite;
        private String environment;
        private Integer parallelThreads = 1;

        // Explicit getters and setters
        public String getScheduleName() {
            return scheduleName;
        }

        public void setScheduleName(String scheduleName) {
            this.scheduleName = scheduleName;
        }

        public String getCronExpression() {
            return cronExpression;
        }

        public void setCronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
        }

        public String getTestSuite() {
            return testSuite;
        }

        public void setTestSuite(String testSuite) {
            this.testSuite = testSuite;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public Integer getParallelThreads() {
            return parallelThreads;
        }

        public void setParallelThreads(Integer parallelThreads) {
            this.parallelThreads = parallelThreads;
        }
    }
}
