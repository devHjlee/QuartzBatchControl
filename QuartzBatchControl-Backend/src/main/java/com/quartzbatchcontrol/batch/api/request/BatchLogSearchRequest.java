package com.quartzbatchcontrol.batch.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchLogSearchRequest {
    private String keyword;
    private String status;
}
