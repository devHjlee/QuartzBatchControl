package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.quartz.domain.QuartzJobHistory;
import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuartzJobHistoryRepository extends JpaRepository<QuartzJobHistory, Long> {
    Optional<QuartzJobHistory> findFirstByJobNameAndJobGroupAndEventTypeOrderByCreatedAtDesc(
            String jobName,
            String jobGroup,
            QuartzJobEventType eventType
    );
}