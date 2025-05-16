package com.quartzbatchcontrol.quartz.domain;

import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quartz_job_history")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuartzJobHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "job_group", nullable = false)
    private String jobGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private QuartzJobEventType eventType; // REGISTER / UPDATE / DELETE

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
