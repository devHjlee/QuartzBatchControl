package com.quartzbatchcontrol.batch.api;

import com.quartzbatchcontrol.batch.api.request.BatchLogSearchRequest;
import com.quartzbatchcontrol.batch.api.response.BatchLogResponse;
import com.quartzbatchcontrol.batch.application.BatchJobLogService;
import com.quartzbatchcontrol.global.response.ApiResponse;

import com.quartzbatchcontrol.quartz.api.response.QuartzLogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/batch-log")
public class BatchLogController {
    private final BatchJobLogService batchJobLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<BatchLogResponse>>> getAllQuartzLogs(
            @ModelAttribute BatchLogSearchRequest request,
            Pageable pageable) {
        Page<BatchLogResponse> contents = batchJobLogService.getAllBatchLogs(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(new PagedModel<>(contents), "Batch 로그 조회를 완료했습니다."));
    }
}
