package com.quartzbatchcontrol.dashboard.application;

import com.quartzbatchcontrol.batch.infrastructure.BatchJobMetaRepository;
import com.quartzbatchcontrol.dashboard.api.response.BatchCountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final BatchJobMetaRepository batchJobMetaRepository;

    public BatchCountResponse getBatchCount() {
        return batchJobMetaRepository.findBatchCount();
    }
}
