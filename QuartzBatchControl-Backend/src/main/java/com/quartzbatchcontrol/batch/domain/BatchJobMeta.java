package com.quartzbatchcontrol.batch.domain;

import com.quartzbatchcontrol.batch.api.request.BatchJobMetaRequest;
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

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "meta_name", nullable = false)
    private String metaName;

    @Column(name = "job_description")
    private String jobDescription;

    @Lob
    private String defaultParams;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void update(String jobDescription, String defaultParams, String updatedBy) {
        this.jobDescription = jobDescription;
        this.defaultParams = defaultParams;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }
}
