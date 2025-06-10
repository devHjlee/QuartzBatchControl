package com.batchcontrol.application;

import com.batchcontrol.domain.BatchJobLog;
import com.batchcontrol.infrastructure.BatchJobLogRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobLogService {
    private final BatchJobLogRepository batchJobLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void startLog(JobExecution jobExecution, String runId) {
        try {
            Long metaId = Long.valueOf(Objects.requireNonNull(jobExecution.getJobParameters().getString("metaId")));
            String executedBy = jobExecution.getJobParameters().getString("executedBy");
            // 시작 시점에 로그 생성
            BatchJobLog log = BatchJobLog.builder()
                    .jobExecutionId(jobExecution.getId())
                    .runId(runId)
                    .jobName(jobExecution.getJobInstance().getJobName())
                    .metaId(metaId)
                    .startTime(LocalDateTime.now())
                    .status("STARTED")
                    .jobParameters(objectMapper.writeValueAsString(jobExecution.getJobParameters().getParameters()))
                    .executedBy(executedBy)
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

    @Transactional
    public void endLog(JobExecution jobExecution) {
        try {
            Long logId = jobExecution.getExecutionContext().getLong("batchJobLogId");
            BatchJobLog jobLog = batchJobLogRepository.findById(logId)
                    .orElseThrow(() -> {
                        log.error("batchJobLogId {} 에 해당하는 로그를 찾을 수 없습니다. jobExecutionId: {}",
                                logId, jobExecution.getId());
                        return new RuntimeException("BatchJobLog not found with id: " + logId);
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
