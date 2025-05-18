package com.quartzbatchcontrol.quartz.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SimpleJobExecutor implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDataMap map = context.getMergedJobDataMap();

            // 파라미터를 다이나믹하게 처리
            map.forEach((key, value) -> {
                log.info("key: {} , value {}",key, value.toString());
            });
        } catch (Exception e) {
            log.error("SimpleJob execution failed", e);
            throw new JobExecutionException(e);
        }
    }
}
