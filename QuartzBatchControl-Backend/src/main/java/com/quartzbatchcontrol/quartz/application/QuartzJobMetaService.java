package com.quartzbatchcontrol.quartz.application;

import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import com.quartzbatchcontrol.quartz.api.request.QuartzJobRequest;
import com.quartzbatchcontrol.quartz.domain.QuartzJobMeta;
import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import com.quartzbatchcontrol.quartz.enums.QuartzJobType;
import com.quartzbatchcontrol.quartz.infrastructure.QuartzJobMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuartzJobMetaService {

    private final QuartzJobMetaRepository quartzJobMetaRepository;

    @Transactional
    public void saveQuartzJobMeta(QuartzJobRequest request, String userName) {
        if (quartzJobMetaRepository.existsByJobName(request.getJobName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL); //todo 에러코드 변경필요
        }

        QuartzJobMeta history = QuartzJobMeta.builder()
                .jobName(request.getJobName())
                .jobGroup(request.getJobGroup())
                .jobType(request.getJobType())
                .eventType(QuartzJobEventType.REGISTER)
                .cronExpression(request.getCronExpression())
                .createdBy(userName)
                .build();

        quartzJobMetaRepository.save(history);
    }

    public void updateQuartzJobMeta(QuartzJobRequest request, String userName) {
        quartzJobMetaRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND))
                .update(request.getEventType(), request.getCronExpression(), request.getMetaId(), userName);

    }

    // todo 삭제이력은?
    public void deleteQuartzJobMeta(QuartzJobRequest request, String userName) {
        QuartzJobMeta quartzJobMeta = quartzJobMetaRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        quartzJobMetaRepository.delete(quartzJobMeta);


    }
}
