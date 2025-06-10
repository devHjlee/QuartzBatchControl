package com.quartzbatchcontrol.batch.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quartzbatchcontrol.batch.api.request.BatchJobMetaRequest;
import com.quartzbatchcontrol.batch.api.response.BatchJobMetaResponse;
import com.quartzbatchcontrol.batch.domain.BatchJobMeta;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobCatalogRepository;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobMetaRepository;

import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.quartzbatchcontrol.batch.api.response.BatchJobDetailResponse;
import com.quartzbatchcontrol.batch.api.response.BatchJobMetaSummaryResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final BatchJobMetaRepository batchJobMetaRepository;
    private final BatchJobCatalogRepository batchJobCatalogRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<BatchJobMetaSummaryResponse> getAllBatchJobMetas(String keyword, Pageable pageable) {
        Page<BatchJobMetaSummaryResponse> rawPage = batchJobMetaRepository.findBySearchCondition(keyword, pageable);

        List<BatchJobMetaSummaryResponse> content = rawPage.getContent().stream()
                .map(raw -> {
                    int count = 0;
                    try {
                        if (raw.getJobParameters() != null) {
                            Map<String, Object> map = objectMapper.readValue(raw.getJobParameters(), new TypeReference<>() {});
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
        return batchJobCatalogRepository.findJobNameByDeletedFalse()
                .stream()
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

        batchJobCatalogRepository
                .findBatchJobCatalogByJobNameAndDeletedFalse(request.getJobName())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_JOB_CLASS));

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
        batchJobCatalogRepository
                .findBatchJobCatalogByJobNameAndDeletedFalse(request.getJobName())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_JOB_CLASS));

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

    @Transactional
    public void executeBatchJob(Long id, String userName) {
        BatchJobMeta batchJobMeta = batchJobMetaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // TODO: 이 경로는 실제 환경에 맞게 설정하거나 외부 설정(application.properties 등)에서 읽어오도록 변경
        String batchJarPath = "externaljob/latest";

        java.io.File jarFile = new java.io.File(batchJarPath);
        if (!jarFile.exists() || !jarFile.isFile()) {
            log.error("배치 JAR 파일을 찾을 수 없습니다: {}", batchJarPath);
            throw new BusinessException(ErrorCode.BATCH_JOB_FAILED, "배치 JAR 파일을 찾을 수 없습니다: " + batchJarPath);
        }

        try {

            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add(batchJarPath);
            command.add("--job.name=" + batchJobMeta.getJobName());
            command.add("--metaId=" + batchJobMeta.getId().toString());
            command.add("--executedBy=" + userName);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // 표준 에러를 표준 출력으로 리다이렉션합니다.

            log.info("커맨드 라인 배치 잡 실행: {}", String.join(" ", command));

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            int exitCode = process.waitFor(); // 프로세스가 종료될 때까지 대기
            log.info("외부 배치 잡 실행 완료. 종료 코드: {}", exitCode);

            if (exitCode != 0) {
                log.error("외부 배치 잡 실행 실패. 출력 내용:\n{}", output.toString());
                // 실패 시, 실행 결과(output)의 일부를 메시지에 포함할 수도 있습니다.
                throw new BusinessException(ErrorCode.BATCH_JOB_FAILED,
                        "외부 배치 잡 실행 실패. 종료 코드: " + exitCode);
            }

            // output.toString().trim(); // 수집된 출력 반환

        } catch (IOException e) {
            log.error("외부 배치 잡 실행 중 IOException 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.BATCH_JOB_FAILED, "외부 배치 잡 시작 또는 결과 읽기 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            log.error("외부 배치 잡 실행 중 InterruptedException 발생: {}", e.getMessage(), e);
            Thread.currentThread().interrupt(); // 현재 스레드의 인터럽트 상태를 복원
            throw new BusinessException(ErrorCode.BATCH_JOB_FAILED, "외부 배치 잡 실행이 중단되었습니다: " + e.getMessage());
        } catch (Exception e) { // 예상치 못한 다른 모든 예외 처리
            log.error("외부 배치 잡 실행 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "외부 배치 잡 실행 중 예상치 못한 오류: " + e.getMessage());
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
//
//    private JobParameters createJobParameters(BatchJobMeta batchJobMeta, String userName) {
//        JobParametersBuilder builder = new JobParametersBuilder()
//                .addLong("timestamp", System.currentTimeMillis());
//
//        builder.addLong("metaId", batchJobMeta.getId());
//        builder.addString("executedBy", userName);
//
//        Map<String, Object> params = deserializeParams(batchJobMeta.getJobParameters());
//        if (params != null) {
//            params.forEach((key, value) -> {
//                if (value instanceof String) {
//                    builder.addString(key, (String) value);
//                } else if (value instanceof Long) {
//                    builder.addLong(key, (Long) value);
//                } else if (value instanceof Integer) {
//                    builder.addLong(key, ((Integer) value).longValue());
//                } else if (value instanceof Double) {
//                    builder.addDouble(key, (Double) value);
//                } else if (value instanceof Boolean) {
//                    builder.addString(key, value.toString());
//                } else {
//                    try {
//                        builder.addString(key, objectMapper.writeValueAsString(value));
//                    } catch (Exception e) {
//                        log.error("파라미터 변환 실패: {}", e.getMessage(), e);
//                        throw new BusinessException(ErrorCode.TYPE_MISMATCH);
//                    }
//                }
//            });
//        }
//
//        return builder.toJobParameters();
//    }
}
