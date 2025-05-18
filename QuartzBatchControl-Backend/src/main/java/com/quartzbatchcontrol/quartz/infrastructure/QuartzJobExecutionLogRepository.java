package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.quartz.domain.QuartzJobExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuartzJobExecutionLogRepository extends JpaRepository<QuartzJobExecutionLog, Long> {
}
