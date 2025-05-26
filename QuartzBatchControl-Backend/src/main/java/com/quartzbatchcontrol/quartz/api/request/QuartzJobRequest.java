package com.quartzbatchcontrol.quartz.api.request;

import com.quartzbatchcontrol.quartz.enums.MisfirePolicy;
import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import com.quartzbatchcontrol.quartz.enums.QuartzJobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuartzJobRequest {

    private Long id;
    private Long batchMetaId;

    @NotBlank(message = "잡 이름은 필수입니다.")
    private String jobName;

    @NotBlank(message = "잡 그룹은 필수입니다.")
    private String jobGroup;

    @NotBlank(message = "Cron 표현식은 필수입니다.")
    private String cronExpression;

    @NotNull(message = "잡 타입은 필수입니다.")
    private QuartzJobType jobType;

    @NotNull(message = "Misfire 정책은 필수입니다.")
    private MisfirePolicy misfirePolicy;

    private QuartzJobEventType eventType;
}
