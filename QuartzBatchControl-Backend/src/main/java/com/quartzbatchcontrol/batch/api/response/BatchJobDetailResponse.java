package com.quartzbatchcontrol.batch.api.response;

import com.quartzbatchcontrol.batch.domain.BatchJobMeta;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchJobDetailResponse {
    private Long id;
    private String jobName;
    private String metaName;
    private String jobDescription;
    private String jobParameters;

    @Builder
    public static BatchJobDetailResponse from(BatchJobMeta entity) {

        return BatchJobDetailResponse.builder()
                .id(entity.getId())
                .jobName(entity.getJobName())
                .metaName(entity.getMetaName())
                .jobParameters(entity.getJobParameters()) // 엔티티의 문자열 그대로 전달
                .build();
    }
} 