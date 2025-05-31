package com.quartzbatchcontrol.batch.api;

import com.quartzbatchcontrol.batch.api.request.BatchJobMetaRequest;
import com.quartzbatchcontrol.batch.api.response.BatchJobMetaResponse;
import com.quartzbatchcontrol.batch.application.BatchJobService;
import com.quartzbatchcontrol.global.response.ApiResponse;
import com.quartzbatchcontrol.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Batch Job", description = "배치 Job 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/batch")
public class BatchJobController {
    private final BatchJobService batchJobService;

    @Operation(summary = "배치 Job 메타데이터 목록 조회", description = "전체 배치 Job 메타데이터 목록을 페이지네이션하여 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagedModel.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<BatchJobMetaSummaryResponse>>> getAllBatchJobMetas(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<BatchJobMetaSummaryResponse> jobMetas = batchJobService.getAllBatchJobMetas(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(new PagedModel<>(jobMetas), "배치 작업 메타데이터 목록 조회를 완료했습니다."));
    }

    @Operation(summary = "배치 Job 상세 조회", description = "특정 배치 Job의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BatchJobDetailResponse.class)))
    })
    @GetMapping("/{metaId}")
    public ResponseEntity<ApiResponse<BatchJobDetailResponse>> getBatchJobDetail(@PathVariable Long metaId) {
        BatchJobDetailResponse jobMeta = batchJobService.getJobParameters(metaId);
        return ResponseEntity.ok(ApiResponse.success(jobMeta, "배치 작업 메타데이터 상세 조회를 완료했습니다."));
    }

    @Operation(summary = "사용 가능한 배치 Job 목록 조회", description = "현재 시스템에 등록된 사용 가능한 배치 Job의 이름 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = String.class)))
    })
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableBatchJobs() {
        List<String> batchJobList = batchJobService.getAvailableBatchJobs();
        return ResponseEntity.ok(ApiResponse.success(batchJobList,"배치 목록 조회를 완료했습니다."));
    }

    @Operation(summary = "모든 배치 Job 메타데이터 조회 (리스트)", description = "전체 배치 Job 메타데이터 목록을 리스트 형태로 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = BatchJobMetaResponse.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BatchJobMetaResponse>>> getAllBatchJobMetas() {
        List<BatchJobMetaResponse> batchJobList = batchJobService.getAllBatchJobMetas();
        return ResponseEntity.ok(ApiResponse.success(batchJobList,"배치 목록 조회를 완료했습니다."));
    }
    @Operation(summary = "배치 Job 등록", description = "새로운 배치 Job을 시스템에 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<String>> saveBatchJob(
            @RequestBody BatchJobMetaRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        batchJobService.saveBatchJob(request, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Batch 등록을 완료 했습니다."));
    }

    @Operation(summary = "배치 Job 수정", description = "기존 배치 Job의 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PutMapping
    public ResponseEntity<ApiResponse<String>> updateBatchJob(
            @RequestBody BatchJobMetaRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        batchJobService.updateBatchJob(request, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Batch 수정을 완료 했습니다."));
    }

    @Operation(summary = "배치 Job 실행", description = "특정 배치 Job을 실행합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "실행 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/execute/{metaId}")
    public ResponseEntity<ApiResponse<String>> executeBatchJob(
            @PathVariable Long metaId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        batchJobService.executeBatchJob(metaId, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Batch Job 수행을 완료 했습니다."));
    }
}
