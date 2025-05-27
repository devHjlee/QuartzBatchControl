package com.quartzbatchcontrol.batch.infrastructure;

import com.quartzbatchcontrol.batch.domain.BatchJobLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobLogRepository extends JpaRepository<BatchJobLog, Long>, BatchJobLogRepositoryCustom {
}
