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
        quartzJobMetaRepository.findById(request.get)
    }

    public QuartzJobType getJobType(String jobName, String jobGroup) {
    return quartzJobMetaRepository
            .findFirstByJobNameAndJobGroupAndEventTypeOrderByCreatedAtDesc(jobName, jobGroup, QuartzJobEventType.REGISTER)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND))
            .getJobType();
    }
}
