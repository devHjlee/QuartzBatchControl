package com.quartzbatchcontrol.quartz.application;

import com.quartzbatchcontrol.quartz.api.response.QuartzJobList;
import com.quartzbatchcontrol.quartz.infrastructure.QuartzJobQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuartzJobQueryService {
    private final QuartzJobQueryRepository quartzJobQueryRepository;

    @Transactional(readOnly = true)
    public List<QuartzJobList> getJobs(String jobName, String jobGroup) {
        return quartzJobQueryRepository.findQuartzJobs();
    }
}
