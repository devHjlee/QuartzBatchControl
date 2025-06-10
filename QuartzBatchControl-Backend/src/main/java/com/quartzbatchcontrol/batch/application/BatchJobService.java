package com.quartzbatchcontrol.batch.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quartzbatchcontrol.batch.api.request.BatchJobMetaRequest;
import com.quartzbatchcontrol.batch.api.response.BatchJobMetaResponse;
import com.quartzbatchcontrol.batch.domain.BatchJobLog;
import com.quartzbatchcontrol.batch.domain.BatchJobMeta;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobCatalogRepository;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobLogRepository;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobMetaRepository;

import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.quartzbatchcontrol.batch.api.response.BatchJobDetailResponse;
import com.quartzbatchcontrol.batch.api.response.BatchJobMetaSummaryResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final BatchJobMetaRepository batchJobMetaRepository;
    private final BatchJobCatalogRepository batchJobCatalogRepository;
    private final BatchJobLogRepository batchJobLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${batch.log.directory}")
    private String logDirectory;

    @Value("${batch.jar.path}")
    private String batchJarPath;

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
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "BatchJobMeta not found: " + id));

        File jarFile = new File(batchJarPath);
        if (!jarFile.exists() || !jarFile.canRead()) {
            throw new BusinessException(ErrorCode.BATCH_JOB_FAILED, "JAR 링크가 깨졌거나 대상 파일이 없습니다.");
        }

        String runId = UUID.randomUUID().toString();

        try {
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add(batchJarPath);
            command.add("--job.name=" + batchJobMeta.getJobName());
            command.add("metaId=" + batchJobMeta.getId());
            command.add("executedBy=" + userName);
            command.add("run.id=" + runId);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            log.info("커맨드 라인 배치 잡 실행: {}", String.join(" ", command));

            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            int exitCode = process.waitFor();
            log.info("외부 배치 잡 실행 완료. runId: {}, 종료 코드: {}", runId, exitCode);

            saveLogFileAndUpdateDb(runId, output.toString());

            if (exitCode != 0) {
                log.error("외부 배치 잡 실행 실패. runId: {}, 출력 내용:\n{}", runId, output.toString());
                throw new BusinessException(ErrorCode.BATCH_JOB_FAILED, "외부 배치 잡 실행 실패. 종료 코드: " + exitCode);
            }

        } catch (IOException e) {
            log.error("외부 배치 잡 실행 중 IOException 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.BATCH_JOB_FAILED, "외부 배치 잡 시작 또는 결과 읽기 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            log.error("외부 배치 잡 실행 중 InterruptedException 발생: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.BATCH_JOB_FAILED, "외부 배치 잡 실행이 중단되었습니다: " + e.getMessage());
        }
    }

    private void saveLogFileAndUpdateDb(String runId, String logContent) {
        Path logFilePath = null;
        try {
            Path logDirPath = Paths.get(logDirectory);
            if (!Files.exists(logDirPath)) {
                Files.createDirectories(logDirPath);
            }

            // 로그 파일 저장
            String logFileName = runId + ".log";
            logFilePath = logDirPath.resolve(logFileName);
            Files.writeString(logFilePath, logContent);
            log.info("배치 로그 파일 저장 완료: {}", logFilePath.toAbsolutePath());

        } catch (IOException e) {
            log.error("로그 파일 저장 실패: {}", logFilePath, e);

        }

        // DB에 로그 경로 업데이트
        final Path finalLogFilePath = logFilePath;
        BatchJobLog batchJobLog = batchJobLogRepository.findByRunId(runId)
                .orElseThrow(() -> {
                    log.error("runId not found: {}", runId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                });

        batchJobLog.updateLogFilePath(finalLogFilePath.toAbsolutePath().toString()); // 엔티티에 경로 업데이트 메소드 필요
        batchJobLogRepository.save(batchJobLog);
        log.info("DB에 로그 파일 경로 업데이트 완료. runId: {}", runId);

    }

    private String serializeParams(Map<String, Object> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            log.error("파라미터 직렬화 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.TYPE_MISMATCH);
        }
    }
}
