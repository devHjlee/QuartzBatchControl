package com.quartzbatchcontrol.batch.api;

import com.quartzbatchcontrol.batch.api.request.BatchJobMetaRequest;
import com.quartzbatchcontrol.batch.application.BatchJobService;
import com.quartzbatchcontrol.global.response.ApiResponse;
import com.quartzbatchcontrol.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.quartzbatchcontrol.batch.api.response.BatchJobParameterResponse;
import com.quartzbatchcontrol.batch.api.response.BatchJobMetaSummaryResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/batch")
public class BatchJobController {
    private final BatchJobService batchJobService;

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

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<String>> executeBatchJob(
            @RequestBody BatchJobMetaRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        batchJobService.executeBatchJob(request.getId(), userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Batch Job 수행을 완료 했습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BatchJobMetaSummaryResponse>>> getAllBatchJobMetas(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String metaName,
            Pageable pageable) {
        Page<BatchJobMetaSummaryResponse> jobMetas = batchJobService.getAllBatchJobMetas(jobName, metaName, pageable);
        return ResponseEntity.ok(ApiResponse.success(jobMetas, "배치 작업 메타데이터 목록 조회를 완료했습니다."));
    }

    @GetMapping("/{metaId}")
    public ResponseEntity<ApiResponse<BatchJobParameterResponse>> getJobParameters(@PathVariable Long metaId) {
        BatchJobParameterResponse jobMeta = batchJobService.getJobParameters(metaId);
        return ResponseEntity.ok(ApiResponse.success(jobMeta, "배치 작업 메타데이터 상세 조회를 완료했습니다."));
    }
}
