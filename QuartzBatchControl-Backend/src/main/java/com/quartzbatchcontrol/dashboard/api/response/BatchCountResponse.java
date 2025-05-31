package com.quartzbatchcontrol.dashboard.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchCountResponse {
    private long batchCount;
    private long registeredCount;
}
