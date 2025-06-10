package com.quartzbatchcontrol.batch.api.response;

import com.quartzbatchcontrol.batch.domain.BatchJobMeta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobMetaResponse {
    private Long id;
    private String jobName;
    private String metaName;

    public static BatchJobMetaResponse from(BatchJobMeta entity) {
        return new BatchJobMetaResponse(entity.getId(), entity.getJobName(), entity.getMetaName());
    }
}
