package com.quartzbatchcontrol.quartz.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class QuartzBatchExecutor implements org.quartz.Job {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDataMap map = context.getMergedJobDataMap();
            String batchJobName = map.getString("batchJobName");

            JobParametersBuilder builder = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis());

            // 파라미터를 다이나믹하게 처리
            map.forEach((key, value) -> {
                if (!key.equals("batchJobName")) {
                    builder.addString(key, value.toString());
                }
            });

            Job batchJob = jobRegistry.getJob(batchJobName);
            JobParameters params = builder.toJobParameters();

            log.info("[Quartz] Launching batch job: {}", batchJobName);
            jobLauncher.run(batchJob, params);

        } catch (Exception e) {
            log.error("Batch execution failed", e);
            throw new JobExecutionException(e);
        }
    }
}
