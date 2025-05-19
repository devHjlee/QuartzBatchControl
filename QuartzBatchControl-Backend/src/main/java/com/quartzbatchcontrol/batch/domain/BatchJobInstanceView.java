package com.quartzbatchcontrol.batch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "BATCH_JOB_INSTANCE")
@Getter
@NoArgsConstructor
public class BatchJobInstanceView {
    @Id
    @Column(name = "JOB_INSTANCE_ID")
    private Long id;

    @Column(name = "JOB_NAME")
    private String jobName;
}
