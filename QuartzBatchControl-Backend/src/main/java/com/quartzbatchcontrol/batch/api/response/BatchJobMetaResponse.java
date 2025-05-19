package com.quartzbatchcontrol.batch.api.response;

import com.quartzbatchcontrol.batch.domain.BatchJobMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchJobMetaResponse {
    private Long id;
    private String jobName;

    private String jobDescription;

    private Map<String, String> defaultParams;

    public static BatchJobMetaResponse from(BatchJobMeta e) {
        return BatchJobMetaResponse.builder()
                .id(e.getId())
                .jobName(e.getJobName())
                .jobDescription(e.getJobDescription())
                .build();
    }
}
