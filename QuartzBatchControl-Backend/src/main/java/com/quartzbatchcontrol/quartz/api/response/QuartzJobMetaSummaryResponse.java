package com.quartzbatchcontrol.quartz.api.response;

import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import com.quartzbatchcontrol.quartz.enums.QuartzJobType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuartzJobMetaSummaryResponse {
    private Long id;
    private String jobName;
    private String jobGroup;
    private QuartzJobType jobType;
    private QuartzJobEventType eventType;
    private Long batchMetaId;
    private String triggerName;
    private String triggerGroup;
    private Long nextFireTime;
    private Long prevFireTime;
    private String triggerState;
    private String createdBy;
} 