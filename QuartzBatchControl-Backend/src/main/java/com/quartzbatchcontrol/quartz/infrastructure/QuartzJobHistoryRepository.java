package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.quartz.domain.QuartzJobHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuartzJobHistoryRepository extends JpaRepository<QuartzJobHistory, Long> {
}