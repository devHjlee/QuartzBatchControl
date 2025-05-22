package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.quartz.domain.QuartzJobMeta;
import org.springframework.data.jpa.repository.JpaRepository;


public interface QuartzJobMetaRepository extends JpaRepository<QuartzJobMeta, Long> {
    boolean existsByJobName(String jobName);
}