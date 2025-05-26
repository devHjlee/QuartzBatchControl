package com.quartzbatchcontrol.quartz.domain;

import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import com.quartzbatchcontrol.quartz.enums.QuartzJobType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quartz_job_meta_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzJobMetaHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meta_id", nullable = false)
    private Long metaId;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "job_group", nullable = false)
    private String jobGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private QuartzJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private QuartzJobEventType eventType;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "batch_meta_id")
    private Long batchMetaId;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public QuartzJobMetaHistory(Long metaId,String jobName, String jobGroup,
                                QuartzJobType jobType, QuartzJobEventType eventType,
                                String cronExpression, Long batchMetaId, String createdBy) {
        this.metaId = metaId;
        this.jobName = jobName;
        this.jobGroup = jobGroup;
        this.jobType = jobType;
        this.eventType = eventType;
        this.cronExpression = cronExpression;
        this.batchMetaId = batchMetaId;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

}
