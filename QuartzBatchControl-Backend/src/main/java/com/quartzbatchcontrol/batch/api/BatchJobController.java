package com.quartzbatchcontrol.batch.api;

import com.quartzbatchcontrol.batch.api.request.BatchJobMetaRequest;
import com.quartzbatchcontrol.batch.application.BatchJobService;
import com.quartzbatchcontrol.global.response.ApiResponse;
import com.quartzbatchcontrol.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.quartzbatchcontrol.batch.api.response.BatchJobDetailResponse;
import com.quartzbatchcontrol.batch.api.response.BatchJobMetaSummaryResponse;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/batch")
public class BatchJobController {
    private final BatchJobService batchJobService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<BatchJobMetaSummaryResponse>>> getAllBatchJobMetas(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String metaName,
            Pageable pageable) {
        Page<BatchJobMetaSummaryResponse> jobMetas = batchJobService.getAllBatchJobMetas(jobName, metaName, pageable);
        return ResponseEntity.ok(ApiResponse.success(new PagedModel<>(jobMetas), "배치 작업 메타데이터 목록 조회를 완료했습니다."));
    }

    @GetMapping("/{metaId}")
    public ResponseEntity<ApiResponse<BatchJobDetailResponse>> getBatchJobDetail(@PathVariable Long metaId) {
        BatchJobDetailResponse jobMeta = batchJobService.getJobParameters(metaId);
        return ResponseEntity.ok(ApiResponse.success(jobMeta, "배치 작업 메타데이터 상세 조회를 완료했습니다."));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableBatchJobs() {
        List<String> batchJobList = batchJobService.getAvailableBatchJobs();
        return ResponseEntity.ok(ApiResponse.success(batchJobList,"배치 목록 조회를 완료했습니다."));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> saveBatchJob(
            @RequestBody BatchJobMetaRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        batchJobService.saveBatchJob(request, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Batch 등록을 완료 했습니다."));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<String>> updateBatchJob(
            @RequestBody BatchJobMetaRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        batchJobService.updateBatchJob(request, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Batch 수정을 완료 했습니다."));
    }

    @PostMapping("/execute/{metaId}")
    public ResponseEntity<ApiResponse<String>> executeBatchJob(
            @PathVariable Long metaId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        batchJobService.executeBatchJob(metaId, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Batch Job 수행을 완료 했습니다."));
    }
}
