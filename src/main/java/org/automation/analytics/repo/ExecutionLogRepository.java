package org.automation.analytics.repo;

import org.automation.analytics.model.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {

    List<ExecutionLog> findBySuiteId(String suiteId);

    List<ExecutionLog> findByStatus(String status);

    List<ExecutionLog> findByTestNameContaining(String testName);
}
