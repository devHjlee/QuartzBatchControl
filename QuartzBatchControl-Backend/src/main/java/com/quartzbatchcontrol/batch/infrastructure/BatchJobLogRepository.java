package com.quartzbatchcontrol.batch.infrastructure;

import com.quartzbatchcontrol.batch.domain.BatchJobLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BatchJobLogRepository extends JpaRepository<BatchJobLog, Long>, BatchJobLogRepositoryCustom {
    Optional<BatchJobLog> findByRunId(String runId);
}
