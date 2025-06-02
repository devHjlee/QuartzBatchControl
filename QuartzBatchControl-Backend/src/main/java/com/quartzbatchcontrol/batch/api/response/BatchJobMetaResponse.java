package com.quartzbatchcontrol.batch.api.response;

import com.quartzbatchcontrol.batch.domain.BatchJobMeta;
import com.quartzbatchcontrol.batch.enums.BatchSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobMetaResponse {
    private Long id;
    private BatchSource batchSource;
    private String jobName;
    private String metaName;

    public static BatchJobMetaResponse from(BatchJobMeta entity) {
        return new BatchJobMetaResponse(entity.getId(), entity.getBatchSource(), entity.getJobName(), entity.getMetaName());
    }
}
