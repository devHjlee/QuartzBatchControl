package com.quartzbatchcontrol.quartz.listener;

import com.quartzbatchcontrol.quartz.application.QuartzJobExecutionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobsListener implements JobListener {
	private final QuartzJobExecutionLogService quartzJobExecutionLogService;

	@Override
	public String getName() {
		return "globalJob";
	}

	/**
	 * Job 수행 전
	 */
	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		context.getJobDetail().getJobDataMap().put("startTime", LocalDateTime.now());
	}

	/**
	 * Job 중단된 상태
	 */
	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {

	}

	/**
	 * Job 수행 완료 후
	 */
	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		String jobName = context.getJobDetail().getKey().getName();
		String jobGroup = context.getJobDetail().getKey().getGroup();
		LocalDateTime startTime = (LocalDateTime) context.getJobDetail().getJobDataMap().get("startTime");
		LocalDateTime endTime = LocalDateTime.now();

		boolean isSuccess = (jobException == null);

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
