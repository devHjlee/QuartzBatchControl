package com.quartzbatchcontrol.batch.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class UserSyncJob {
    
    @Bean(name = "userSyncJobConfig")
    public Job userSyncJob(JobRepository jobRepository,
                          Step userSyncStep) {
        return new JobBuilder("userSyncJob", jobRepository)
                .start(userSyncStep)
                .build();
    }

    @Bean
    @JobScope
    public Step userSyncStep(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager) {
        return new StepBuilder("userSyncStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> [UserSync] 동기화 작업 시작");
                    // TODO: 실제 사용자 동기화 로직 구현
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
} 