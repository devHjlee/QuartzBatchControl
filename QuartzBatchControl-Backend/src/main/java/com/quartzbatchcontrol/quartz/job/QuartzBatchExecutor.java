package com.quartzbatchcontrol.quartz.job;

import com.quartzbatchcontrol.batch.application.BatchJobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.JobExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuartzBatchExecutor implements org.quartz.Job {

    @Autowired
    private BatchJobService batchJobService;

    public QuartzBatchExecutor() {
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDataMap map = context.getMergedJobDataMap();
            batchJobService.executeBatchJob(map.getLong("batchMetaId"), "quartz");

            log.info("[Quartz] Launching batch job: {}", map.getLong("batchMetaId"));

        } catch (Exception e) {
            log.error("Batch execution failed", e);
            throw new JobExecutionException(e);
        }
    }
}
