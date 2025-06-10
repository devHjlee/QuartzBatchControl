package com.batchcontrol.listener;

import com.batchcontrol.application.BatchJobLogService;
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
        String runId = jobExecution.getJobParameters().getString("run.id");
        jobExecution.getJobParameters().getParameters().forEach((k, v) -> {
            log.info("parameter {} = {}", k, v.getValue());
        });
        if (runId == null) {
            log.error("JobParameter에 'run.id'가 존재하지 않습니다. 외부 시스템에 의한 실행이 아닐 수 있습니다.");
            // 혹은 이 실행을 실패처리하고 싶다면 예외를 던질 수 있습니다.
            // throw new IllegalArgumentException("'run.id' is required in JobParameters.");
        }
        batchJobLogService.startLog(jobExecution, runId);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        batchJobLogService.endLog(jobExecution);
    }
}
