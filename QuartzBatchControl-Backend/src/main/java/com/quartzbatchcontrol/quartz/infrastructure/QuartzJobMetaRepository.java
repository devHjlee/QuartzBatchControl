package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.quartz.domain.QuartzJobMeta;
import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuartzJobMetaRepository extends JpaRepository<QuartzJobMeta, Long> {
    Optional<QuartzJobMeta> findFirstByJobNameAndJobGroupAndEventTypeOrderByCreatedAtDesc(
            String jobName,
            String jobGroup,
            QuartzJobEventType eventType
    );
}