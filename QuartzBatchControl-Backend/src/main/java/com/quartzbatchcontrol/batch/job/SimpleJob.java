package com.quartzbatchcontrol.batch.job;

import com.quartzbatchcontrol.batch.listener.BatchJobExecutionListener;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SimpleJob {
    private final BatchJobExecutionListener batchJobExecutionListener;
    // 1. Job 정의
    @Bean(name = "simpleBatchJobConfig")
    public Job simpleBatchJob(JobRepository jobRepository,
                         Step simpleStepOne,
                         Step simpleStepTwo) {
        return new JobBuilder("simpleBatchJob", jobRepository)
                .start(simpleStepOne)
                .next(simpleStepTwo)
                .listener(batchJobExecutionListener)
                .build();
    }

    // 2. Step 1 정의 - 파라미터 받기
    @Bean
    @JobScope
    public Step simpleStepOne(@Value("#{jobParameters['param1']}") String param1,
                              @Value("#{jobParameters['param2']}") String param2,
                              JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleStepOne", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> [STEP 1] param1: {}, param2: {}", param1, param2);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // 3. Step 2 정의 - 완료 로그
    @Bean
    public Step simpleStepTwo(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleStepTwo", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> [STEP 2] 작업 완료");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
