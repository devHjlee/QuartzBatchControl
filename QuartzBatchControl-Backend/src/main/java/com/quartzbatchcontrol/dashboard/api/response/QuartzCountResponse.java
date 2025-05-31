package com.quartzbatchcontrol.dashboard.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuartzCountResponse {
    private long quartzCount;
    private long waitCount;
    private long pausedCount;
    private long CompletedCount;
}
