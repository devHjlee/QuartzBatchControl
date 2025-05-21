package com.quartzbatchcontrol.batch.api;

import com.quartzbatchcontrol.batch.api.request.BatchJobMetaRequest;
import com.quartzbatchcontrol.batch.api.response.BatchJobListResponse;
import com.quartzbatchcontrol.batch.application.BatchJobService;
import com.quartzbatchcontrol.global.response.ApiResponse;
import com.quartzbatchcontrol.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/batch")
public class BatchJobController {
    private final BatchJobService batchJobService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createBatchJob(
            @RequestBody BatchJobMetaRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        batchJobService.createBatchJob(request, userPrincipal.getUsername());

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
    public ResponseEntity<ApiResponse<List<BatchJobListResponse>>> getBatchJobs(@RequestParam String jobName) {
        return ResponseEntity.ok(ApiResponse.success(batchJobService.getBatchJobs(jobName),"Batch Job 조회 완료"));
    }
}
