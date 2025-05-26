package com.quartzbatchcontrol.quartz.application;

import com.quartzbatchcontrol.batch.infrastructure.BatchJobMetaRepository;
import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import com.quartzbatchcontrol.quartz.api.request.QuartzJobRequest;
import com.quartzbatchcontrol.quartz.domain.QuartzJobMeta;
import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import com.quartzbatchcontrol.quartz.enums.QuartzJobType;
import com.quartzbatchcontrol.quartz.job.QuartzBatchExecutor;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.quartzbatchcontrol.quartz.enums.QuartzJobType.*;

@Service
@RequiredArgsConstructor
public class QuartzJobService {

    private final Scheduler scheduler;
    private final QuartzJobMetaService quartzJobMetaService;
    private final BatchJobMetaRepository batchJobMetaRepository;

    /**
     * 공통 Job 등록
     */
    private void scheduleJob(QuartzJobRequest request) {
        JobDataMap dataMap = new JobDataMap();

        Class<? extends Job> jobClass;

        if (BATCH.equals(request.getJobType())) {
            jobClass = QuartzBatchExecutor.class;
            dataMap.put("batchMetaId", request.getBatchMetaId());
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
            if (request.getBatchMetaId() == null) {
                throw new BusinessException(ErrorCode.MISSING_PARAMETER);
            }
            if (!batchJobMetaRepository.existsById(request.getBatchMetaId())) {
                throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
            }
        }
        quartzJobMetaService.saveQuartzJobMeta(request, userName);

        scheduleJob(request);
    }

    @Transactional
    public void updateJob(QuartzJobRequest request, String userName) {
        QuartzJobMeta quartzJobMeta = quartzJobMetaService.getQuartzJobMeta(request.getId());
        JobKey jobKey = new JobKey(quartzJobMeta.getJobName(), quartzJobMeta.getJobGroup());

        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }

            if (QuartzJobType.BATCH.equals(request.getJobType())) {
                if (request.getBatchMetaId() == null) {
                    throw new BusinessException(ErrorCode.MISSING_PARAMETER, "batchMetaId는 필수입니다.");
                }
                if (!batchJobMetaRepository.existsById(request.getBatchMetaId())) {
                    throw new BusinessException(ErrorCode.INVALID_PARAMETER, "존재하지 않는 batchMetaId입니다.");
                }
            }

            quartzJobMetaService.updateQuartzJobMeta(request);
            quartzJobMetaService.saveQuartzJobMetaHistory(request.getId(), QuartzJobEventType.UPDATE, request.getCronExpression(), userName);

            scheduler.deleteJob(jobKey);
            scheduleJob(request);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void deleteJob(Long metaId, String userName) {
        QuartzJobMeta quartzJobMeta = quartzJobMetaService.getQuartzJobMeta(metaId);
        JobKey jobKey = new JobKey(quartzJobMeta.getJobName(), quartzJobMeta.getJobGroup());

        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }

            quartzJobMetaService.saveQuartzJobMetaHistory(quartzJobMeta.getId(), QuartzJobEventType.DELETE, quartzJobMeta.getCronExpression(), userName);
            quartzJobMetaService.deleteQuartzJobMeta(quartzJobMeta.getId());


            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void pauseJob(Long metaId, String userName) {
        QuartzJobMeta quartzJobMeta = quartzJobMetaService.getQuartzJobMeta(metaId);
        JobKey jobKey = new JobKey(quartzJobMeta.getJobName(), quartzJobMeta.getJobGroup());

        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }

            quartzJobMetaService.saveQuartzJobMetaHistory(quartzJobMeta.getId(), QuartzJobEventType.PAUSE, quartzJobMeta.getCronExpression(), userName);

            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void resumeJob(Long metaId, String userName) {
        QuartzJobMeta quartzJobMeta = quartzJobMetaService.getQuartzJobMeta(metaId);
        JobKey jobKey = new JobKey(quartzJobMeta.getJobName(), quartzJobMeta.getJobGroup());

        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }

            quartzJobMetaService.saveQuartzJobMetaHistory(quartzJobMeta.getId(), QuartzJobEventType.RESUME, quartzJobMeta.getCronExpression(), userName);

            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    @Transactional
    public void triggerJobNow(Long metaId, String userName) {
        QuartzJobMeta quartzJobMeta = quartzJobMetaService.getQuartzJobMeta(metaId);
        JobKey jobKey = new JobKey(quartzJobMeta.getJobName(), quartzJobMeta.getJobGroup());

        try {
            if (!scheduler.checkExists(jobKey)) {
                throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
            }

            quartzJobMetaService.saveQuartzJobMetaHistory(quartzJobMeta.getId(), QuartzJobEventType.NOW, quartzJobMeta.getCronExpression(), userName);

            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            throw new BusinessException(ErrorCode.QUARTZ_SCHEDULING_FAILED, e);
        }
    }

    /**
     * 주어진 Cron 표현식에 대한 다음 실행 시간 목록을 반환합니다.
     * @param cronExpression Quartz Cron 표현식 문자열
     * @param numTimes 반환할 다음 실행 시간의 개수
     * @return 다음 실행 시간(타임스탬프) 목록
     */
    public List<Long> getNextFireTimes(String cronExpression, int numTimes) {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new BusinessException(ErrorCode.INVALID_CRON_EXPRESSION);
        }

        List<Long> nextFireTimes = new ArrayList<>();
        try {
            CronExpression cron = new CronExpression(cronExpression);
            Date nextFireTime = new Date(); // 현재 시간부터 시작

            for (int i = 0; i < numTimes; i++) {
                Date newTime = cron.getNextValidTimeAfter(nextFireTime);
                if (newTime == null) { // 더 이상 유효한 다음 실행 시간이 없을 경우
                    break;
                }
                nextFireTimes.add(newTime.getTime());
                nextFireTime = newTime; // 다음 계산을 위해 현재 시간을 업데이트
            }
        } catch (ParseException e) {
            // CronExpression.isValidExpression 에서 이미 체크하지만,
            // new CronExpression(cronExpression) 에서도 ParseException 발생 가능성 있음
            throw new BusinessException(ErrorCode.INVALID_CRON_EXPRESSION, "Cron 표현식 파싱 중 오류 발생: " + e.getMessage());
        }
        return nextFireTimes;
    }
}