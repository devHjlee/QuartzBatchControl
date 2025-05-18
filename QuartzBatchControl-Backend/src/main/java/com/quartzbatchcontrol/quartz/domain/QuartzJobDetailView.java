package com.quartzbatchcontrol.quartz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Entity
@Immutable
@IdClass(com.quartzbatchcontrol.quartz.domain.QuartzJobDetailId.class)
@Table(name = "QRTZ_JOB_DETAILS")
@Getter
@NoArgsConstructor
public class QuartzJobDetailView {

    @Id
    @Column(name = "JOB_NAME", columnDefinition = "varchar(190) not null")
    private String jobName;

    @Id
    @Column(name = "JOB_GROUP", columnDefinition = "varchar(190) not null")
    private String jobGroup;

    @Column(name = "DESCRIPTION", columnDefinition = "varchar(250)")
    private String description;

    @Column(name = "JOB_CLASS_NAME", columnDefinition = "varchar(250) not null")
    private String jobClassName;

    @Column(name = "IS_DURABLE", columnDefinition = "varchar(1) not null")
    private String isDurable;

    @Column(name = "IS_NONCONCURRENT", columnDefinition = "varchar(1) not null")
    private String isNonConcurrent;

    @Column(name = "IS_UPDATE_DATA", columnDefinition = "varchar(1) not null")
    private String isUpdateData;

    @Lob
    @Column(name = "JOB_DATA", columnDefinition = "blob")
    private byte[] jobData;
}

class QuartzJobDetailId implements Serializable {
    private String jobName;
    private String jobGroup;
}
