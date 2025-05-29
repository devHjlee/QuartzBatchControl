package com.quartzbatchcontrol.quartz.listener;

import com.quartzbatchcontrol.quartz.application.QuartzJobExecutionLogService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Lazy; // Spring의 @Lazy 임포트
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class JobsListener implements JobListener {
	private final QuartzJobExecutionLogService quartzJobExecutionLogService;
	private final Scheduler scheduler;

	public JobsListener(QuartzJobExecutionLogService quartzJobExecutionLogService, @Lazy Scheduler scheduler) {
		this.quartzJobExecutionLogService = quartzJobExecutionLogService;
		this.scheduler = scheduler;
	}

	@Override
	public String getName() {
		return "globalJob";
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		context.getJobDetail().getJobDataMap().put("startTime", LocalDateTime.now());
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		String jobName = context.getJobDetail().getKey().getName();
		String jobGroup = context.getJobDetail().getKey().getGroup();
		LocalDateTime startTime = (LocalDateTime) context.getJobDetail().getJobDataMap().get("startTime");
		LocalDateTime endTime = LocalDateTime.now();
		boolean isSuccess = (jobException == null);

		if (!isSuccess) {
			log.error("Job {} in group {} failed with exception: {}", jobName, jobGroup, jobException.getMessage(), jobException);
			TriggerKey triggerKey = context.getTrigger().getKey();
			try {
				if (scheduler != null) {
					scheduler.pauseTrigger(triggerKey);
					log.info("Trigger {} in group {} has been paused due to a job execution exception.", triggerKey.getName(), triggerKey.getGroup());
				} else {
					log.error("Scheduler was not injected into JobsListener, cannot pause trigger.");
				}
			} catch (SchedulerException e) {
				log.error("Failed to pause trigger {} in group {}: {}", triggerKey.getName(), triggerKey.getGroup(), e.getMessage(), e);
			}
		}

		quartzJobExecutionLogService.save(
				jobName,
				jobGroup,
				startTime,
				endTime,
				isSuccess ? "SUCCESS" : "FAIL",
				isSuccess ? null : jobException.getMessage()
		);
	}
}