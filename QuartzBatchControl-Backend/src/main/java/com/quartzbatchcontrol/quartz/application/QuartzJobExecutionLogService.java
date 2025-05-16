package com.quartzbatchcontrol.quartz.application;

import com.quartzbatchcontrol.quartz.domain.QuartzJobExecutionLog;
import com.quartzbatchcontrol.quartz.infrastructure.QuartzJobExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuartzJobExecutionLogService {
    private final QuartzJobExecutionLogRepository repository;

    public void save(String jobName,
                     String jobGroup,
                     LocalDateTime startTime,
                     LocalDateTime endTime,
                     String status,
                     String message) {

        QuartzJobExecutionLog log = QuartzJobExecutionLog.builder()
                .jobName(jobName)
                .jobGroup(jobGroup)
                .startTime(startTime)
                .endTime(endTime)
                .status(status)
                .message(message)
                .build();

        repository.save(log);
    }
}
