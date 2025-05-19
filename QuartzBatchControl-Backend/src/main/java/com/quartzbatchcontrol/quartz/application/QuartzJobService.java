package com.quartzbatchcontrol.quartz.application;

import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import com.quartzbatchcontrol.quartz.api.request.QuartzJobRequest;
import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import com.quartzbatchcontrol.quartz.enums.QuartzJobType;
import com.quartzbatchcontrol.quartz.job.QuartzBatchExecutor;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.quartzbatchcontrol.quartz.enums.QuartzJobType.*;

@Service
@RequiredArgsConstructor
public class QuartzJobService {

    private final Scheduler scheduler;
    private final QuartzJobHistoryService quartzJobHistoryService;

    /**
     * 공통 Job 등록
     */
    private void scheduleJob(QuartzJobRequest request) {
        JobDataMap dataMap = request.getParameters() != null ? request.getParameters() : new JobDataMap();

        Class<? extends Job> jobClass;

        if (BATCH.equals(request.getJobType())) {
            jobClass = QuartzBatchExecutor.class;
            dataMap.put("batchJobName", request.getBatchJobName());
        } else if (SIMPLE.equals(request.getJobType())) {
            try {
                jobClass = (Class<Job>) Class.forName("com.quartzbatchcontrol.quartz.job." + request.getJobName());
            } catch (ClassNotFoundException e) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }
        } else {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        if (!CronExpression.isValidExpression(request.getCronExpression())) {
            throw new BusinessException(ErrorCode.INVALID_CRON_EXPRESSION);
        }

        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(request.getJobName(), request.getJobGroup())
                .usingJobData(dataMap)
                .build();

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(request.getCronExpression());

        switch (request.getMisfirePolicy()) {
            case FIRE_AND_PROCEED -> scheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
            case DO_NOTHING -> scheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionDoNothing();
            case IGNORE -> scheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
            default -> scheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
        }

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(request.getJobName() + "Trigger", request.getJobGroup())
                .withSchedule(scheduleBuilder)
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void createJob(QuartzJobRequest request, String userName) {
        quartzJobHistoryService.saveHistory(
                request.getJobName(),
                request.getJobGroup(),
                request.getJobType(),
                QuartzJobEventType.REGISTER,
                request.getCronExpression(),
                userName
        );

        scheduleJob(request);
    }

    @Transactional
    public void updateJob(QuartzJobRequest request, String userName) {
        JobKey jobKey = new JobKey(request.getJobName(), request.getJobGroup());

        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }

        quartzJobHistoryService.saveHistory(
                request.getJobName(),
                request.getJobGroup(),
                request.getJobType(),
                QuartzJobEventType.UPDATE,
                request.getCronExpression(),
                userName
        );

        try {
            scheduler.deleteJob(jobKey);
            scheduleJob(request);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void deleteJob(String jobName, String jobGroup, String userName) {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        QuartzJobType jobType;

        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }
            jobType = quartzJobHistoryService.getJobType(jobName, jobGroup);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }

        quartzJobHistoryService.saveHistory(
                jobName,
                jobGroup,
                jobType,
                QuartzJobEventType.DELETE,
                null,
                userName
        );

        try {
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void pauseJob(String jobName, String jobGroup, String userName) {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        QuartzJobType jobType;
        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }
            jobType = quartzJobHistoryService.getJobType(jobName, jobGroup);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }

        quartzJobHistoryService.saveHistory(
                jobName, jobGroup, jobType, QuartzJobEventType.PAUSE, null, userName
        );

        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void resumeJob(String jobName, String jobGroup, String userName) {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        QuartzJobType jobType;
        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }
            jobType = quartzJobHistoryService.getJobType(jobName, jobGroup);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }

        quartzJobHistoryService.saveHistory(
                jobName, jobGroup, jobType, QuartzJobEventType.RESUME, null, userName
        );

        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void triggerJobNow(String jobName, String jobGroup, String userName) {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        QuartzJobType jobType;
        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }
            jobType = quartzJobHistoryService.getJobType(jobName, jobGroup);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }

        quartzJobHistoryService.saveHistory(
                jobName, jobGroup, jobType, QuartzJobEventType.TRIGGER_NOW, null, userName
        );

        try {
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }
}