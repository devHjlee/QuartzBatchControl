package com.batchcontrol.infrastructure;

import com.batchcontrol.domain.BatchJobLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobLogRepository extends JpaRepository<BatchJobLog, Long> {
}
