package com.quartzbatchcontrol.batch.application;

import com.quartzbatchcontrol.batch.api.request.BatchLogSearchRequest;
import com.quartzbatchcontrol.batch.api.response.BatchLogResponse;
import com.quartzbatchcontrol.batch.domain.BatchJobLog;
import com.quartzbatchcontrol.batch.infrastructure.BatchJobLogRepository;
import com.quartzbatchcontrol.global.exception.BusinessException;
import com.quartzbatchcontrol.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobLogService {

    private final BatchJobLogRepository batchJobLogRepository;

    @Transactional(readOnly = true)
    public Page<BatchLogResponse> getAllBatchLogs(BatchLogSearchRequest request, Pageable pageable) {
        return batchJobLogRepository.findBySearchLog(request, pageable);
    }

    @Transactional(readOnly = true)
    public String getLogContentByRunId(String runId) {
        BatchJobLog batchLog = batchJobLogRepository.findByRunId(runId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "해당 runId의 로그를 찾을 수 없습니다: " + runId));

        if (batchLog.getFilePath() == null || batchLog.getFilePath().isBlank()) {
            return "저장된 로그 파일 경로가 없습니다.";
        }

        try {
            // TODO: 외부저장소일 경우 수정 필요
            Path path = Paths.get(batchLog.getFilePath());
            if (!Files.exists(path)) {
                log.warn("DB에 경로가 있으나 실제 로그 파일을 찾을 수 없습니다: {}", batchLog.getFilePath());
                return "로그 파일을 찾을 수 없습니다: " + batchLog.getFilePath();
            }
            return Files.readString(path);
        } catch (IOException e) {
            log.error("로그 파일을 읽는 중 오류 발생: {}", batchLog.getFilePath(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "로그 파일을 읽는 중 오류가 발생했습니다.");
        }
    }
}
