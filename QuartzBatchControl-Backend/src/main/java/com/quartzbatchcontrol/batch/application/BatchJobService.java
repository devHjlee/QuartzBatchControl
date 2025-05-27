package com.quartzbatchcontrol.batch.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quartzbatchcontrol.batch.api.request.BatchJobMetaRequest;
import com.quartzbatchcontrol.batch.api.response.BatchJobMetaResponse;
import com.quartzbatchcontrol.batch.domain.BatchJobMeta;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobMetaRepository;

import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.quartzbatchcontrol.batch.api.response.BatchJobDetailResponse;
import com.quartzbatchcontrol.batch.api.response.BatchJobMetaSummaryResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final BatchJobMetaRepository batchJobMetaRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<BatchJobMetaSummaryResponse> getAllBatchJobMetas(String keyword, Pageable pageable) {
        Page<BatchJobMetaSummaryResponse> rawPage = batchJobMetaRepository.findBySearchCondition(keyword, pageable);

        ObjectMapper mapper = new ObjectMapper();

        List<BatchJobMetaSummaryResponse> content = rawPage.getContent().stream()
                .map(raw -> {
                    int count = 0;
                    try {
                        if (raw.getJobParameters() != null) {
                            Map<String, Object> map = mapper.readValue(raw.getJobParameters(), new TypeReference<>() {});
                            count = map.size();
                        }
                    } catch (Exception e) {
                        log.warn("파라미터 파싱 실패: {}", raw.getJobParameters(), e); // todo 코드추가
                    }

                    return BatchJobMetaSummaryResponse.builder()
                            .id(raw.getId())
                            .jobName(raw.getJobName())
                            .metaName(raw.getMetaName())
                            .jobDescription(raw.getJobDescription())
                            .jobParameters(raw.getJobParameters())
                            .createdBy(raw.getCreatedBy())
                            .createdAt(raw.getCreatedAt())
                            .updatedBy(raw.getUpdatedBy())
                            .updatedAt(raw.getUpdatedAt())
                            .isRegisteredInQuartz(raw.isRegisteredInQuartz())
                            .jobParameterSize(count)
                            .build();
                }).toList();
        return new PageImpl<>(content, pageable, rawPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public BatchJobDetailResponse getJobParameters(Long metaId) {
        BatchJobMeta batchJobMeta = batchJobMetaRepository.findById(metaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                        "BatchJobMeta not found with id: " + metaId));
        return BatchJobDetailResponse.from(batchJobMeta);
    }

    public List<String> getAvailableBatchJobs() {
        return jobRegistry.getJobNames().stream()
                .sorted()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BatchJobMetaResponse> getAllBatchJobMetas() {
        return batchJobMetaRepository.findAll()
                .stream()
                .map(BatchJobMetaResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveBatchJob(BatchJobMetaRequest request, String userName) {
        if (batchJobMetaRepository.existsByJobNameAndMetaName(request.getJobName(), request.getMetaName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL); //todo 코드추가필요
        }
        if (!isValidJob(request.getJobName())) {
            throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
        }

        try {
            batchJobMetaRepository.save(BatchJobMeta.builder()
                    .jobName(request.getJobName())
                    .metaName(request.getMetaName())
                    .jobDescription(request.getJobDescription())
                    .jobParameters(serializeParams(request.getJobParameters()))
                    .createdBy(userName)
                    .createdAt(LocalDateTime.now())
                    .updatedBy(userName)
                    .updatedAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("배치 작업 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void updateBatchJob(BatchJobMetaRequest request, String userName) {
        BatchJobMeta batchJobMeta = batchJobMetaRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 작업 존재 여부 검증
        if (!isValidJob(batchJobMeta.getJobName())) {
            throw new BusinessException(ErrorCode.INVALID_JOB_CLASS);
        }

        try {
            String serializedParams = request.getJobParameters() != null ?
                    serializeParams(request.getJobParameters()) :
                    batchJobMeta.getJobParameters();

            batchJobMeta.update(
                    request.getMetaName(),
                    request.getJobDescription(),
                    serializedParams,
                    userName
            );

        } catch (Exception e) {
            log.error("배치 작업 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void executeBatchJob(Long id, String userName) {
        BatchJobMeta batchJobMeta = batchJobMetaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        try {
            Job job = jobRegistry.getJob(batchJobMeta.getJobName());
            JobParameters jobParameters = createJobParameters(batchJobMeta);
            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            log.error("배치 작업 실행 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isValidJob(String jobName) {
        try {
            jobRegistry.getJob(jobName);
            return true;
        } catch (NoSuchJobException e) {
            throw new BusinessException(ErrorCode.INVALID_JOB_CLASS, e);
        }
    }

    private String serializeParams(Map<String, Object> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            log.error("파라미터 직렬화 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.TYPE_MISMATCH);
        }
    }

    private Map<String, Object> deserializeParams(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("파라미터 역직렬화 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.TYPE_MISMATCH);
        }
    }

    private JobParameters createJobParameters(BatchJobMeta batchJobMeta) {
        JobParametersBuilder builder = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis());

        builder.addLong("metaId", batchJobMeta.getId());

        Map<String, Object> params = deserializeParams(batchJobMeta.getJobParameters());
        if (params != null) {
            params.forEach((key, value) -> {
                if (value instanceof String) {
                    builder.addString(key, (String) value);
                } else if (value instanceof Long) {
                    builder.addLong(key, (Long) value);
                } else if (value instanceof Integer) {
                    builder.addLong(key, ((Integer) value).longValue());
                } else if (value instanceof Double) {
                    builder.addDouble(key, (Double) value);
                } else if (value instanceof Boolean) {
                    builder.addString(key, value.toString());
                } else {
                    try {
                        builder.addString(key, objectMapper.writeValueAsString(value));
                    } catch (Exception e) {
                        log.error("파라미터 변환 실패: {}", e.getMessage(), e);
                        throw new BusinessException(ErrorCode.TYPE_MISMATCH);
                    }
                }
            });
        }

        return builder.toJobParameters();
    }
}
