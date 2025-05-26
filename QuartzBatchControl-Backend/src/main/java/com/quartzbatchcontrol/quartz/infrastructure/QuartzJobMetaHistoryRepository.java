package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.quartz.domain.QuartzJobMetaHistory;
import org.springframework.data.jpa.repository.JpaRepository;


public interface QuartzJobMetaHistoryRepository extends JpaRepository<QuartzJobMetaHistory, Long> {
}