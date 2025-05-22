package com.quartzbatchcontrol.quartz.domain;

import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import com.quartzbatchcontrol.quartz.enums.QuartzJobType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quartz_job_meta")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzJobMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "job_group", nullable = false)
    private String jobGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private QuartzJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private QuartzJobEventType eventType; // REGISTER / UPDATE / DELETE

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "meta_id")
    private Long metaId; // batch_job_meta 테이블의 key

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public QuartzJobMeta(String jobName, String jobGroup,
                          QuartzJobType jobType, QuartzJobEventType eventType,
                          String cronExpression, String createdBy) {
        this.jobName = jobName;
        this.jobGroup = jobGroup;
        this.jobType = jobType;
        this.eventType = eventType;
        this.cronExpression = cronExpression;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedBy = createdBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void update(QuartzJobEventType eventType, String cronExpression, Long metaId, String userName) {
        this.eventType = eventType;
        this.cronExpression = cronExpression;
        this.metaId = metaId;
        this.updatedBy = userName;
        this.updatedAt = LocalDateTime.now();
    }
}
