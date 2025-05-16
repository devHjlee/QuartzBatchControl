package com.quartzbatchcontrol.quartz.application;

import com.quartzbatchcontrol.quartz.domain.QuartzJobHistory;
import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;
import com.quartzbatchcontrol.quartz.infrastructure.QuartzJobHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuartzJobHistoryService {

    private final QuartzJobHistoryRepository historyRepository;

    public void saveHistory(String jobName,
                            String jobGroup,
                            QuartzJobEventType eventType,
                            String cronExpression,
                            String createdBy) {

        QuartzJobHistory history = QuartzJobHistory.builder()
                .jobName(jobName)
                .jobGroup(jobGroup)
                .eventType(eventType)
                .cronExpression(cronExpression)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);
    }
}
