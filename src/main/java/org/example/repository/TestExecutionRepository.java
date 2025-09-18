package org.example.repository;

import org.example.model.TestExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecution, Long> {

    List<TestExecution> findByExecutionId(String executionId);

    List<TestExecution> findByStatus(TestExecution.ExecutionStatus status);

    List<TestExecution> findByEnvironmentOrderByStartTimeDesc(String environment);

    List<TestExecution> findByEnvironment(String environment);

    List<TestExecution> findTop10ByOrderByStartTimeDesc();

    @Query("SELECT te FROM TestExecution te WHERE te.startTime BETWEEN :startDate AND :endDate ORDER BY te.startTime DESC")
    List<TestExecution> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(te) FROM TestExecution te WHERE te.status = :status AND te.startTime >= :fromDate")
    Long countByStatusAndDateAfter(@Param("status") TestExecution.ExecutionStatus status, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT te FROM TestExecution te WHERE te.testCase.id = :testCaseId ORDER BY te.startTime DESC")
    List<TestExecution> findByTestCaseIdOrderByStartTimeDesc(@Param("testCaseId") Long testCaseId);

    @Query("SELECT te FROM TestExecution te WHERE te.testBatch.id = :testBatchId ORDER BY te.startTime DESC")
    List<TestExecution> findByTestBatchId(@Param("testBatchId") Long testBatchId);

    // Add missing methods for cleanup functionality
    List<TestExecution> findByStartTimeBefore(LocalDateTime cutoffDate);

    @Query("SELECT te FROM TestExecution te WHERE te.startTime BETWEEN :startTime AND :endTime")
    List<TestExecution> findByStartTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
