package com.quartzbatchcontrol.quartz.api;

import com.quartzbatchcontrol.global.response.ApiResponse;
import com.quartzbatchcontrol.quartz.api.request.QuartzLogSearchRequest;
import com.quartzbatchcontrol.quartz.api.response.QuartzLogResponse;
import com.quartzbatchcontrol.quartz.application.QuartzJobExecutionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Quartz Log", description = "Quartz Log 관련 API")
@RestController
@RequestMapping("/api/quartz-log")
@RequiredArgsConstructor
public class QuartzLogController {
    private final QuartzJobExecutionLogService quartzJobExecutionLogService;

    @Operation(summary = "Quartz Log 목록 조회", description = "Quartz 실행 Log 목록을 페이지네이션하여 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagedModel.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<QuartzLogResponse>>> getAllQuartzLogs(
            @ModelAttribute QuartzLogSearchRequest request,
            Pageable pageable) {
        Page<QuartzLogResponse> contents = quartzJobExecutionLogService.getAllQuartzLogs(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(new PagedModel<>(contents), "Quartz 로그 조회를 완료했습니다."));
    }
}
