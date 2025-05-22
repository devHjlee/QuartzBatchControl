package com.quartzbatchcontrol.batch.infrastructure;

import com.quartzbatchcontrol.batch.api.response.BatchJobMetaSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BatchJobMetaRepositoryCustom {
    Page<BatchJobMetaSummaryResponse> findBySearchCondition(String jobName, String metaName, Pageable pageable);
} 