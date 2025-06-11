package com.quartzbatchcontrol.batch.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchLogResponse {
    private Long id;
    private String runId;
    private Long jobExecutionId;
    private String jobName;
    private Long metaId;
    private String batchName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String exitCode;
    private String exitMessage;
    private String filePath;
    private String jobParameters;
}
