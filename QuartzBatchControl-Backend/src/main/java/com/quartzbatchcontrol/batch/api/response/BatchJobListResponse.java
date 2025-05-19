package com.quartzbatchcontrol.batch.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BatchJobListResponse {

    private final Long id;
    private final String jobName;
    private final String jobDescription;
    private final String defaultParameters;
    private final LocalDateTime lastExecutedAt;
    private final String lastStatus;
    private final String lastFailureMessage;
    private final Long executionCount;
    private final Long successCount;
    private final Long failureCount;
    private final LocalDateTime createdAt;
    private final String createdBy;
    private final LocalDateTime updatedAt;
    private final String updatedBy;
}
