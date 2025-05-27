package com.quartzbatchcontrol.quartz.api.response;

import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import com.quartzbatchcontrol.quartz.enums.QuartzJobType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuartzJobMetaSummaryResponse {
    private Long id;
    private String jobName;
    private String jobGroup;
    private QuartzJobType jobType;
    private Long batchMetaId;
    private String triggerName;
    private String triggerGroup;
    private Long nextFireTime;
    private Long prevFireTime;
    private String triggerState;
    private String cronExpression;
    private String batchName;
    private String createdBy;
} 