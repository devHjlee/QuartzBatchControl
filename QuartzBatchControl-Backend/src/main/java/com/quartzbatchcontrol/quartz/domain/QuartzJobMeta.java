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

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "batch_meta_id")
    private Long batchMetaId; // batch_job_meta 테이블의 key

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @Builder
    public QuartzJobMeta(String jobName, String jobGroup,
                          QuartzJobType jobType, Long batchMetaId,
                          String cronExpression, String createdBy) {
        this.jobName = jobName;
        this.jobGroup = jobGroup;
        this.jobType = jobType;
        this.batchMetaId = batchMetaId;
        this.cronExpression = cronExpression;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public void update(String cronExpression) {
        this.cronExpression = cronExpression;
    }
}
