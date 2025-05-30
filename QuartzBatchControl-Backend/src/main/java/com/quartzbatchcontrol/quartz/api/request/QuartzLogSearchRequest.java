package com.quartzbatchcontrol.quartz.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class QuartzLogSearchRequest {
    private String keyword;
    private String jobGroup;
    private String status;
}
