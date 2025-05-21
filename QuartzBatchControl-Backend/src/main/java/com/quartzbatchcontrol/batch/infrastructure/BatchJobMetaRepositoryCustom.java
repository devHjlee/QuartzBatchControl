package com.quartzbatchcontrol.batch.infrastructure;

import com.quartzbatchcontrol.batch.domain.BatchJobMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BatchJobMetaRepositoryCustom {
    Page<BatchJobMeta> findBySearchCondition(String jobName, String metaName, Pageable pageable);
} 