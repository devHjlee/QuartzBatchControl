package com.quartzbatchcontrol.batch.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobMetaRequest {
    Long id;

    @NotBlank(message = "잡 이름은 필수입니다.")
    private String jobName;

    private String jobDescription;

    // ex: {"date":"2025-01-01", "type":"daily"}
    private Map<String, String> defaultParams;
}
