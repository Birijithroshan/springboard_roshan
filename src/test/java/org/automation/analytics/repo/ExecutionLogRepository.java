//package org.automation.analytics.repo;
//
//import org.automation.analytics.model.ExecutionLog;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.time.LocalDate;
//import java.util.List;
//
//public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {
//
//    List<ExecutionLog> findBySuiteId(String suiteId);
//
//    @Query("SELECT COUNT(e) FROM ExecutionLog e WHERE e.startTime >= ?1 AND e.startTime <= ?2")
//    long countByDateRange(java.time.LocalDateTime from, java.time.LocalDateTime to);
//
//    // Additional custom queries for trends etc can be implemented in service using native query or JPA query
//}
