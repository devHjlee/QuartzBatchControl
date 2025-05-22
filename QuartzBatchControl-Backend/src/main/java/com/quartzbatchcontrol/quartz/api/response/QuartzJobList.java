package com.quartzbatchcontrol.quartz.api.response;

import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import com.quartzbatchcontrol.quartz.enums.QuartzJobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuartzJobList {
    private QuartzJobType jobType;
    private QuartzJobEventType eventType;
    private String metaName;
    private String metaId;
    private String jobName;
    private String jobGroup;
    private String state;
    private String cronExpression;
    private Long nextFireTime;
    private Long previousFireTime;

    private String createdBy;
}
