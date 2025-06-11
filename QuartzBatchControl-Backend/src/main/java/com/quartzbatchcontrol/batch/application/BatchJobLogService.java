package com.quartzbatchcontrol.batch.application;

import com.quartzbatchcontrol.batch.api.request.BatchLogSearchRequest;
import com.quartzbatchcontrol.batch.api.response.BatchLogResponse;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobLogService {
    private final BatchJobLogRepository batchJobLogRepository;

    @Transactional(readOnly = true)
    public Page<BatchLogResponse> getAllBatchLogs(BatchLogSearchRequest request, Pageable pageable) {
        return batchJobLogRepository.findBySearchLog(request, pageable);
    }

}
