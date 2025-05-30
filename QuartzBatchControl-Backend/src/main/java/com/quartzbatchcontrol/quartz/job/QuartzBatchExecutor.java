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

        } catch (Exception e) {
            log.error("QuartzBatchExecutor: Caught exception from BatchJobService. Type: {}, Message: {}" , e.getClass().getName(), e.getMessage(), e);
            JobExecutionException jee = new JobExecutionException(e);
            log.error("QuartzBatchExecutor: Throwing JobExecutionException. Original exception: ", e);
            throw jee;
        }
    }
}
