package com.quartzbatchcontrol.batch.api.request;

import com.quartzbatchcontrol.batch.enums.BatchSource;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class BatchJobMetaRequest {
    Long id;

    @NotBlank(message = "배치 실행 구분값은 필수입니다.")
    private BatchSource batchSource;

    @NotBlank(message = "잡 이름은 필수입니다.")
    private String jobName;

    @NotBlank(message = "작업 설정명은 필수입니다.")
    private String metaName;

    private String jobDescription;

    // ex: {"date":"2025-01-01", "type":"daily", "isActive": true, "threshold": 100.5}
    private Map<String, Object> jobParameters;
}
