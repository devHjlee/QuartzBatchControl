package com.batchcontrol.listener;

import com.batchcontrol.application.BatchJobLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class BatchJobExecutionListener implements JobExecutionListener, ExitCodeGenerator {
    private final BatchJobLogService batchJobLogService;
    private int exitCode = 0;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String runId = jobExecution.getJobParameters().getString("runId");
        if (runId == null) {
            log.error("JobParameter에 'run.id'가 존재하지 않습니다. 외부 시스템에 의한 실행이 아닐 수 있습니다.");
            throw new RuntimeException("JobParameter에 'run.id'가 존재하지 않습니다. 외부 시스템에 의한 실행이 아닐 수 있습니다.");
        }
        batchJobLogService.startLog(jobExecution, runId);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (!"COMPLETED".equals(jobExecution.getStatus())) this.exitCode = 1;
        batchJobLogService.endLog(jobExecution);
    }

    @Override
    public int getExitCode() {
        return this.exitCode;
    }
}
