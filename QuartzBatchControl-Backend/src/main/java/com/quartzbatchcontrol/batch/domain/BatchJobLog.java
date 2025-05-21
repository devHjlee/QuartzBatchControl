package com.quartzbatchcontrol.batch.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_job_execution_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchJobLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long jobExecutionId;

    @Column(nullable = false)
    private String jobName;

    @Column(nullable = false)
    private Long metaId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(nullable = false)
    private String status;

    private String exitCode;

    private String exitMessage;

    @Column(columnDefinition = "TEXT")
    private String jobParameters;

    @Builder
    public BatchJobLog(Long jobExecutionId, String jobName, Long metaId, LocalDateTime startTime,
                       LocalDateTime endTime, String status, String exitCode,
                       String exitMessage, String jobParameters) {
        this.jobExecutionId = jobExecutionId;
        this.jobName = jobName;
        this.metaId = metaId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.exitCode = exitCode;
        this.exitMessage = exitMessage;
        this.jobParameters = jobParameters;
    }

    public void update(String status, String exitCode, String exitMessage) {
        this.status = status;
        this.exitCode = exitCode;
        this.exitMessage = exitMessage;
        this.endTime = LocalDateTime.now();
    }
}