package com.quartzbatchcontrol.batch.listener;

import com.quartzbatchcontrol.batch.application.BatchJobLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;


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
