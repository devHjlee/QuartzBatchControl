package com.quartzbatchcontrol.batch.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quartzbatchcontrol.batch.api.request.BatchJobMetaRequest;
import com.quartzbatchcontrol.batch.api.response.BatchJobListResponse;
import com.quartzbatchcontrol.batch.api.response.BatchJobMetaResponse;
import com.quartzbatchcontrol.batch.domain.BatchJobMeta;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobMetaRepository;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobQueryRepository;
import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final BatchJobMetaRepository batchJobMetaRepository;
    private final BatchJobQueryRepository batchJobQueryRepository;

    public void createBatchJob(BatchJobMetaRequest request, String userName) {
        if (isValidJob(request.getJobName())) {
            batchJobMetaRepository.save(BatchJobMeta.builder()
                            .jobName(request.getJobName())
                            .jobDescription(request.getJobDescription())
                            .defaultParams(toJson(request.getDefaultParams()))
                            .createdBy(userName)
                            .createdAt(LocalDateTime.now())
                            .updatedBy(userName)
                            .updatedAt(LocalDateTime.now())
                            .build());
        }
    }

    public void runBatchJob(Long id, String userName) {
        BatchJobMeta batchJobMeta = batchJobMetaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        try {
            Job job = jobRegistry.getJob(batchJobMeta.getJobName());

            JobParametersBuilder builder = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()); // 유니크 보장

            Map<String, String> params = toMap(batchJobMeta.getDefaultParams());

            if (params != null) {
                params.forEach(builder::addString);
            }

            JobParameters jobParameters = builder.toJobParameters();
            jobLauncher.run(job, jobParameters);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    public List<BatchJobListResponse> getBatchJobs(String jobName) {
        return batchJobQueryRepository.findBatchJobsWithSummary();
    }

    private boolean isValidJob(String jobName) {
        try {
            jobRegistry.getJob(jobName);
            return true;
        } catch (NoSuchJobException e) {
            throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
        }
    }

    private String toJson(Map<String, String> map) {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("파라미터 직렬화 실패", e);
        }
    }

    private Map<String, String> toMap(String json) {
        try {
            return new ObjectMapper().readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("파라미터 역직렬화 실패", e);
        }
    }

}
