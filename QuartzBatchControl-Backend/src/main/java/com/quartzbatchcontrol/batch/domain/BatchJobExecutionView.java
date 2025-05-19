package com.quartzbatchcontrol.batch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "BATCH_JOB_EXECUTION")
@Getter
@NoArgsConstructor
public class BatchJobExecutionView {

    @Id
    @Column(name = "JOB_EXECUTION_ID")
    private Long id;

    @Column(name = "JOB_INSTANCE_ID")
    private Long jobInstanceId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "EXIT_MESSAGE")
    private String exitMessage;

    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
}
