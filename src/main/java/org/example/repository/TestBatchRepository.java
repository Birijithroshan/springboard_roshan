package org.example.repository;

import org.example.model.TestBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestBatchRepository extends JpaRepository<TestBatch, Long> {

    Optional<TestBatch> findByBatchId(String batchId);

    List<TestBatch> findByEnvironmentOrderByCreatedAtDesc(String environment);

    @Query("SELECT tb FROM TestBatch tb WHERE tb.scheduledTime BETWEEN :startDate AND :endDate ORDER BY tb.scheduledTime")
    List<TestBatch> findByScheduledTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT tb FROM TestBatch tb WHERE tb.status IN ('SCHEDULED', 'RUNNING') ORDER BY tb.scheduledTime")
    List<TestBatch> findActiveBatches();

    // Enhanced methods for recent batch loading
    @Query("SELECT tb FROM TestBatch tb ORDER BY tb.createdAt DESC")
    List<TestBatch> findAllOrderByCreatedAtDesc();

    @Query("SELECT tb FROM TestBatch tb WHERE tb.createdAt >= :since ORDER BY tb.createdAt DESC")
    List<TestBatch> findRecentBatches(@Param("since") LocalDateTime since);

    // Add missing method for weekly report generation
    @Query("SELECT tb FROM TestBatch tb WHERE tb.startTime BETWEEN :startTime AND :endTime ORDER BY tb.startTime DESC")
    List<TestBatch> findByStartTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query(value = "SELECT * FROM test_batches ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<TestBatch> findTopRecentBatches(@Param("limit") int limit);

    @Query("SELECT tb FROM TestBatch tb WHERE tb.status = :status ORDER BY tb.createdAt DESC")
    List<TestBatch> findByStatusOrderByCreatedAtDesc(@Param("status") TestBatch.BatchStatus status);
}
