package com.quartzbatchcontrol.batch.domain;

import com.quartzbatchcontrol.batch.enums.BatchSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_job_meta")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BatchJobMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "batch_source", nullable = false, length = 20)
    private BatchSource batchSource;

    @Column(name = "meta_name", nullable = false)
    private String metaName;
    
    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "job_description")
    private String jobDescription;

    @Column(columnDefinition = "TEXT")
    private String jobParameters;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void update(String metaName, String jobDescription, String jobParameters, String updatedBy) {
        this.metaName = metaName;
        this.jobDescription = jobDescription;
        this.jobParameters = jobParameters;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }
}
