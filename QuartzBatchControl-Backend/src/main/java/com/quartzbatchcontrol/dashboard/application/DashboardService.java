package com.quartzbatchcontrol.dashboard.application;

import com.quartzbatchcontrol.batch.infrastructure.BatchJobLogRepository;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobMetaRepository;
import com.quartzbatchcontrol.dashboard.api.response.BatchCountResponse;
import com.quartzbatchcontrol.dashboard.api.response.DailyStatusCountResponse;
import com.quartzbatchcontrol.dashboard.api.response.QuartzCountResponse;
import com.quartzbatchcontrol.quartz.infrastructure.QuartzJobExecutionLogRepository;
import com.quartzbatchcontrol.quartz.infrastructure.QuartzJobMetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final BatchJobMetaRepository batchJobMetaRepository;
    private final BatchJobLogRepository batchJobLogRepository;
    private final QuartzJobExecutionLogRepository quartzJobExecutionLogRepository;
    private final QuartzJobMetaRepository quartzJobMetaRepository;


    public BatchCountResponse getBatchCount() {
        return batchJobMetaRepository.findBatchCount();
    }

    public DailyStatusCountResponse getBatchLogCount() {
        return batchJobLogRepository.findBatchLogCount();
    }

    public QuartzCountResponse getQuartzCount() {
        return quartzJobMetaRepository.findQuartzJobCount();
    }

    public DailyStatusCountResponse getQuartzLogCount() {
        return quartzJobExecutionLogRepository.findQuartzStateCount();
    }
}
