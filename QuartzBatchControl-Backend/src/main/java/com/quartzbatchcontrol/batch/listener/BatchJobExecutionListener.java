package com.quartzbatchcontrol.batch.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quartzbatchcontrol.batch.application.BatchJobLogService;
import com.quartzbatchcontrol.batch.application.BatchJobService;
import com.quartzbatchcontrol.batch.domain.BatchJobLog;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobLogRepository;
import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchJobExecutionListener implements JobExecutionListener {
    private final BatchJobLogService batchJobLogService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        batchJobLogService.startLog(jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        batchJobLogService.endLog(jobExecution);
    }
}
