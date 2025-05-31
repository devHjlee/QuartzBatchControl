package com.quartzbatchcontrol.batch.infrastructure;

import com.quartzbatchcontrol.batch.api.request.BatchLogSearchRequest;
import com.quartzbatchcontrol.batch.api.response.BatchLogResponse;
import com.quartzbatchcontrol.dashboard.api.response.DailyStatusCountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BatchJobLogRepositoryCustom {
    Page<BatchLogResponse> findBySearchLog(BatchLogSearchRequest request, Pageable pageable);
    DailyStatusCountResponse findBatchLogCount();
}
