package com.quartzbatchcontrol.batch.api.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BatchJobMetaSummaryResponse {
    private final Long id;
    private final String jobName;
    private final String metaName;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private final String updatedBy;
    private final LocalDateTime updatedAt;
    private final boolean isRegisteredInQuartz;

    public BatchJobMetaSummaryResponse(
            Long id,
            String jobName,
            String metaName,
            String createdBy,
            LocalDateTime createdAt,
            String updatedBy,
            LocalDateTime updatedAt,
            boolean isRegisteredInQuartz) {
        this.id = id;
        this.jobName = jobName;
        this.metaName = metaName;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.isRegisteredInQuartz = isRegisteredInQuartz;
    }
} 