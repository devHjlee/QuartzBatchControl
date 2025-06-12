package com.quartzbatchcontrol.quartz.application;

import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import com.quartzbatchcontrol.quartz.api.request.QuartzJobRequest;
import com.quartzbatchcontrol.quartz.api.response.QuartzJobMetaSummaryResponse;
import com.quartzbatchcontrol.quartz.domain.QuartzJobMeta;
import com.quartzbatchcontrol.quartz.domain.QuartzJobMetaHistory;
import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import com.quartzbatchcontrol.quartz.infrastructure.QuartzJobMetaHistoryRepository;
import com.quartzbatchcontrol.quartz.infrastructure.QuartzJobMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class QuartzJobMetaService {

    private final QuartzJobMetaRepository quartzJobMetaRepository;
    private final QuartzJobMetaHistoryRepository quartzJobMetaHistoryRepository;

    @Transactional(readOnly = true)
    public Page<QuartzJobMetaSummaryResponse> getAllQuartzJobMetas(String keyword, Pageable pageable) {
        return quartzJobMetaRepository.findBySearchCondition(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public QuartzJobMeta getQuartzJobMeta(Long id) {
        return quartzJobMetaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    }

    @Transactional
    public void saveQuartzJobMeta(QuartzJobRequest request, String userName) {
        if (quartzJobMetaRepository.existsByJobName(request.getJobName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_QUARTZ_JOB);
        }

        QuartzJobMeta jobMeta = quartzJobMetaRepository.save(QuartzJobMeta.builder()
                .jobName(request.getJobName())
                .jobGroup(request.getJobGroup())
                .jobType(request.getJobType())
                .batchMetaId(request.getBatchMetaId())
                .cronExpression(request.getCronExpression())
                .createdBy(userName)
                .build());
        saveQuartzJobMetaHistory(jobMeta.getId(), QuartzJobEventType.REGISTER, request.getCronExpression(), userName);
    }

    @Transactional
    public void updateQuartzJobMeta(QuartzJobRequest request) {
        quartzJobMetaRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND))
                .update(request.getCronExpression());
    }

    @Transactional
    public void deleteQuartzJobMeta(Long metaId) {
        QuartzJobMeta quartzJobMeta = quartzJobMetaRepository.findById(metaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        quartzJobMetaRepository.delete(quartzJobMeta);

    }

    @Transactional
    public void saveQuartzJobMetaHistory(Long metaId, QuartzJobEventType eventType, String cronExpression, String userName) {
        QuartzJobMeta quartzJobMeta = quartzJobMetaRepository.findById(metaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        quartzJobMetaHistoryRepository.save(QuartzJobMetaHistory.builder()
                .metaId(metaId)
                .jobName(quartzJobMeta.getJobName())
                .jobGroup(quartzJobMeta.getJobGroup())
                .jobType(quartzJobMeta.getJobType())
                .eventType(eventType)
                .cronExpression(cronExpression)
                .batchMetaId(quartzJobMeta.getBatchMetaId())
                .createdBy(userName)
                .build());
    }
}
