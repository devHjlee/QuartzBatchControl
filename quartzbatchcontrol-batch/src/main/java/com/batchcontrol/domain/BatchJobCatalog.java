package com.batchcontrol.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_job_catalog")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchJobCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jobName;

    @Column(nullable = false)
    private boolean deleted;

    @Builder
    public BatchJobCatalog(String jobName, boolean deleted) {
        this.jobName = jobName;
        this.deleted = deleted;
    }

}