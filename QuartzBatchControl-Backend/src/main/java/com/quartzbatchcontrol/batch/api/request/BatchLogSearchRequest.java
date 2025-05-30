package com.quartzbatchcontrol.batch.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class BatchLogSearchRequest {
    private String keyword;
    private String status;
}
