package com.quartzbatchcontrol.batch.api;

import com.quartzbatchcontrol.batch.api.request.BatchLogSearchRequest;
import com.quartzbatchcontrol.batch.api.response.BatchLogResponse;
import com.quartzbatchcontrol.batch.application.BatchJobLogService;
import com.quartzbatchcontrol.global.response.ApiResponse;

import com.quartzbatchcontrol.quartz.api.response.QuartzLogResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Batch Log", description = "배치 Log 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/batch-log")
public class BatchLogController {
    private final BatchJobLogService batchJobLogService;

    @Operation(summary = "배치 Log 목록 조회", description = "배치 실행 Log 목록을 페이지네이션하여 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagedModel.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<BatchLogResponse>>> getAllQuartzLogs(
            @ModelAttribute BatchLogSearchRequest request,
            Pageable pageable) {
        Page<BatchLogResponse> contents = batchJobLogService.getAllBatchLogs(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(new PagedModel<>(contents), "Batch 로그 조회를 완료했습니다."));
    }
}
