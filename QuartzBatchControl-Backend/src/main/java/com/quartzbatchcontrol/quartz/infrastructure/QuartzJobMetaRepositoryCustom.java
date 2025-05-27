package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.quartz.api.response.QuartzJobMetaSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuartzJobMetaRepositoryCustom {
    Page<QuartzJobMetaSummaryResponse> findBySearchCondition(String keyword, Pageable pageable);
}
