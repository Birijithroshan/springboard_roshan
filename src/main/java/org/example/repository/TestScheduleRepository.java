package org.example.repository;

import org.example.model.TestSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestScheduleRepository extends JpaRepository<TestSchedule, Long> {

    List<TestSchedule> findByIsActiveTrue();

    List<TestSchedule> findByEnvironment(String environment);

    List<TestSchedule> findByTestSuite(String testSuite);

    @Query("SELECT s FROM TestSchedule s WHERE s.isActive = true AND s.nextExecution <= :currentTime")
    List<TestSchedule> findDueSchedules(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT s FROM TestSchedule s WHERE s.isActive = true AND s.nextExecution BETWEEN :startTime AND :endTime")
    List<TestSchedule> findSchedulesBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
