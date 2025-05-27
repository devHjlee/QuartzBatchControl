package com.quartzbatchcontrol.quartz.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuartzLogResponse {
    private Long id;
    private String jobName;
    private String jobGroup;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String message;
}
