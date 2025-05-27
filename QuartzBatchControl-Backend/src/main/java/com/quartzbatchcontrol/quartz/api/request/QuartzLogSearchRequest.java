package com.quartzbatchcontrol.quartz.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuartzLogSearchRequest {
    private String jobName;
    private String jobGroup;
    private String status;
}
