package com.quartzbatchcontrol.dashboard.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatusCountResponse {
    private long successCount;
    private long failureCount;
}
