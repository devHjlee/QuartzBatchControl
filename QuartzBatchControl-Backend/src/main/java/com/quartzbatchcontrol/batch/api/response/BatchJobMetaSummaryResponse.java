package com.quartzbatchcontrol.batch.api.response;

import com.quartzbatchcontrol.batch.enums.BatchSource;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BatchJobMetaSummaryResponse {
    private final Long id;
    private final BatchSource batchSource;
    private final String jobName;
    private final String metaName;
    private final String jobDescription;
    private final String jobParameters;
    private final int jobParameterSize;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private final String updatedBy;
    private final LocalDateTime updatedAt;
    private final boolean isRegisteredInQuartz;

    public BatchJobMetaSummaryResponse(
            Long id,
            BatchSource batchSource,
            String jobName,
            String metaName,
            String jobDescription,
            String jobParameters,
            int jobParameterSize,
            String createdBy,
            LocalDateTime createdAt,
            String updatedBy,
            LocalDateTime updatedAt,
            boolean isRegisteredInQuartz) {
        this.id = id;
        this.batchSource = batchSource;
        this.jobName = jobName;
        this.metaName = metaName;
        this.jobDescription = jobDescription;
        this.jobParameters = jobParameters;
        this.jobParameterSize = jobParameterSize;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.isRegisteredInQuartz = isRegisteredInQuartz;
    }
}