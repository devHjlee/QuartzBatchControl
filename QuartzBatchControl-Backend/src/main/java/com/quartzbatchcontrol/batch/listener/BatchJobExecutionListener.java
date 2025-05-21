package com.quartzbatchcontrol.batch.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quartzbatchcontrol.batch.domain.BatchJobLog;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobLogRepository;
import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchJobExecutionListener implements JobExecutionListener {
    private final BatchJobLogRepository batchJobLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        try {
            Long metaId = jobExecution.getJobParameters().getLong("metaId");

            // 시작 시점에 로그 생성
            BatchJobLog log = BatchJobLog.builder()
                    .jobExecutionId(jobExecution.getId())
                    .jobName(jobExecution.getJobInstance().getJobName())
                    .metaId(metaId)
                    .startTime(LocalDateTime.now())
                    .status("STARTED")
                    .jobParameters(objectMapper.writeValueAsString(jobExecution.getJobParameters().getParameters()))
                    .build();

            // 로그 저장 후 JobExecution에 로그 ID 저장
            BatchJobLog savedLog = batchJobLogRepository.save(log);
            jobExecution.getExecutionContext().put("batchJobLogId", savedLog.getId());
        } catch (Exception e) {
            log.error("배치 작업 시작 로그 기록 실패: jobName={}, metaId={}, error={}",
                    jobExecution.getJobInstance().getJobName(),
                    jobExecution.getJobParameters().getLong("metaId"),
                    e.getMessage(), e);
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        try {
            Long logId = jobExecution.getExecutionContext().getLong("batchJobLogId");
            BatchJobLog jobLog = batchJobLogRepository.findById(logId)
                    .orElseThrow(() -> {
                        log.error("batchJobLogId {} 에 해당하는 로그를 찾을 수 없습니다. jobExecutionId: {}",
                                logId, jobExecution.getId());
                        return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "BatchJobLog not found with id: " + logId);
                    });
            jobLog.update(
                    jobExecution.getStatus().toString(),
                    jobExecution.getExitStatus().getExitCode(),
                    jobExecution.getExitStatus().getExitDescription()
                    );

        } catch (Exception e) {
            log.error("배치 작업 종료 로그 기록 실패: jobName={}, metaId={}, error={}",
                    jobExecution.getJobInstance().getJobName(),
                    jobExecution.getJobParameters().getLong("metaId"),
                    e.getMessage(), e);
        }
    }
}
