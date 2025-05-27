package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.quartz.api.request.QuartzLogSearchRequest;
import com.quartzbatchcontrol.quartz.api.response.QuartzLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuartzLogExecutionRepositoryCustom {
    Page<QuartzLogResponse> findBySearchLog(QuartzLogSearchRequest searchRequest, Pageable pageable);
}
