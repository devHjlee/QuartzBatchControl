package com.quartzbatchcontrol.quartz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Entity
@Immutable
@IdClass(com.quartzbatchcontrol.quartz.domain.QuartzCronTriggerId.class)
@Table(name = "QRTZ_CRON_TRIGGERS")
@Getter
@NoArgsConstructor
public class QuartzCronTriggerView {

    @Id
    @Column(name = "TRIGGER_NAME", columnDefinition = "varchar(190) not null")
    private String triggerName;

    @Id
    @Column(name = "TRIGGER_GROUP", columnDefinition = "varchar(190) not null")
    private String triggerGroup;

    @Column(name = "CRON_EXPRESSION", columnDefinition = "varchar(120) not null")
    private String cronExpression;

    @Column(name = "TIME_ZONE_ID", columnDefinition = "varchar(80)")
    private String timeZoneId;
}

class QuartzCronTriggerId implements Serializable {
    private String triggerName;
    private String triggerGroup;
}
