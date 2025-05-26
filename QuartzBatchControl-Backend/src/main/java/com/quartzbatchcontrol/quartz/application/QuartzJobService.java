package com.quartzbatchcontrol.quartz.application;

import com.quartzbatchcontrol.batch.infrastructure.BatchJobMetaRepository;
import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import com.quartzbatchcontrol.quartz.api.request.QuartzJobRequest;
import com.quartzbatchcontrol.quartz.api.response.QuartzJobMetaSummaryResponse;
import com.quartzbatchcontrol.quartz.enums.QuartzJobType;
import com.quartzbatchcontrol.quartz.infrastructure.QuartzJobMetaRepository;
import com.quartzbatchcontrol.quartz.job.QuartzBatchExecutor;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.quartzbatchcontrol.quartz.enums.QuartzJobType.*;

@Service
@RequiredArgsConstructor
public class QuartzJobService {

    private final Scheduler scheduler;
    private final QuartzJobMetaService quartzJobMetaService;
    private final QuartzJobMetaRepository quartzJobMetaRepository;
    private final BatchJobMetaRepository batchJobMetaRepository;


    @Transactional(readOnly = true)
    public Page<QuartzJobMetaSummaryResponse> getAllQuartzJobMetas(String keyword, Pageable pageable) {
        return quartzJobMetaRepository.findBySearchCondition(keyword, pageable);
    }

    /**
     * 공통 Job 등록
     */
    private void scheduleJob(QuartzJobRequest request) {
        JobDataMap dataMap = new JobDataMap();

        Class<? extends Job> jobClass;

        if (BATCH.equals(request.getJobType())) {
            jobClass = QuartzBatchExecutor.class;
            dataMap.put("metaId", request.getMetaId());
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
        if (QuartzJobType.BATCH.equals(request.getJobType())) {
            if (request.getMetaId() == null) {
                throw new BusinessException(ErrorCode.MISSING_PARAMETER);
            }
            if (!batchJobMetaRepository.existsById(request.getMetaId())) {
                throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
            }
        }
        quartzJobMetaService.saveQuartzJobMeta(request, userName);

        scheduleJob(request);
    }

    @Transactional
    public void updateJob(QuartzJobRequest request, String userName) {
        JobKey jobKey = new JobKey(request.getJobName(), request.getJobGroup());

        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }

            if (QuartzJobType.BATCH.equals(request.getJobType())) {
                if (request.getMetaId() == null) {
                    throw new BusinessException(ErrorCode.MISSING_PARAMETER, "metaId는 필수입니다.");
                }
                if (!batchJobMetaRepository.existsById(request.getMetaId())) {
                    throw new BusinessException(ErrorCode.INVALID_PARAMETER, "존재하지 않는 metaId입니다.");
                }
            }

            quartzJobMetaService.updateQuartzJobMeta(request, userName);

            scheduler.deleteJob(jobKey);
            scheduleJob(request);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void deleteJob(QuartzJobRequest request, String userName) {
        JobKey jobKey = new JobKey(request.getJobName(), request.getJobGroup());

        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }
            quartzJobMetaService.deleteQuartzJobMeta(request, userName);

            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void pauseJob(QuartzJobRequest request, String userName) {
        JobKey jobKey = new JobKey(request.getJobName(), request.getJobGroup());

        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }
            quartzJobMetaService.updateQuartzJobMeta(request, userName);

            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void resumeJob(QuartzJobRequest request, String userName) {
        JobKey jobKey = new JobKey(request.getJobName(), request.getJobGroup());

        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }
            quartzJobMetaService.updateQuartzJobMeta(request, userName);

            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void triggerJobNow(QuartzJobRequest request, String userName) {
        JobKey jobKey = new JobKey(request.getJobName(), request.getJobGroup());

        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }
            quartzJobMetaService.updateQuartzJobMeta(request, userName);

            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }
}