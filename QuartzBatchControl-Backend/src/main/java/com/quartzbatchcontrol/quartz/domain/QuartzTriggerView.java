package com.quartzbatchcontrol.quartz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Entity
@Immutable
@IdClass(com.quartzbatchcontrol.quartz.domain.QuartzTriggerId.class)
@Table(name = "QRTZ_TRIGGERS")
@Getter
@NoArgsConstructor
public class QuartzTriggerView {

    @Id
    @Column(name = "TRIGGER_NAME", columnDefinition = "varchar(190) not null")
    private String triggerName;

    @Id
    @Column(name = "TRIGGER_GROUP", columnDefinition = "varchar(190) not null")
    private String triggerGroup;

    @Column(name = "JOB_NAME", columnDefinition = "varchar(190) not null")
    private String jobName;

    @Column(name = "JOB_GROUP", columnDefinition = "varchar(190) not null")
    private String jobGroup;

    @Column(name = "TRIGGER_STATE", columnDefinition = "varchar(16) not null")
    private String triggerState;

    @Column(name = "NEXT_FIRE_TIME")
    private Long nextFireTime;

    @Column(name = "PREV_FIRE_TIME")
    private Long previousFireTime;
}

class QuartzTriggerId implements Serializable {
    private String triggerName;
    private String triggerGroup;
}