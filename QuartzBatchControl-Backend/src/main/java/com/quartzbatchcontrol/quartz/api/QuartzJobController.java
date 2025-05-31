package com.quartzbatchcontrol.quartz.api;

import com.quartzbatchcontrol.global.response.ApiResponse;
import com.quartzbatchcontrol.global.security.UserPrincipal;
import com.quartzbatchcontrol.quartz.api.request.QuartzJobRequest;
import com.quartzbatchcontrol.quartz.api.response.QuartzJobMetaSummaryResponse;
import com.quartzbatchcontrol.quartz.application.QuartzJobMetaService;
import com.quartzbatchcontrol.quartz.application.QuartzJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Quartz Job", description = "Quartz Job 관련 API")
@RestController
@RequestMapping("/api/quartz-jobs")
@RequiredArgsConstructor
public class QuartzJobController {

    private final QuartzJobService quartzJobService;
    private final QuartzJobMetaService quartzJobMetaService;

    @Operation(summary = "Quartz Job 메타데이터 목록 조회", description = "전체 Quartz Job 메타데이터 목록을 페이지네이션하여 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagedModel.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<QuartzJobMetaSummaryResponse>>> getAllQuartzJobMetas(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<QuartzJobMetaSummaryResponse> jobMetas = quartzJobMetaService.getAllQuartzJobMetas(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(new PagedModel<>(jobMetas), "배치 작업 메타데이터 목록 조회를 완료했습니다."));
    }

    @Operation(summary = "CRON 표현식 다음 실행 시간 미리보기", description = "주어진 CRON 표현식에 대한 다음 실행 시간들을 미리봅니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = Long.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/preview-schedule")
    public ResponseEntity<ApiResponse<List<Long>>> getNextFireTimes(
            @RequestParam String cronExpression,
            @RequestParam(defaultValue = "5") int numTimes) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CRON_EXPRESSION_REQUIRED", "cronExpression 파라미터는 필수입니다."));
        }
        List<Long> nextFireTimes = quartzJobService.getNextFireTimes(cronExpression, numTimes);
        return ResponseEntity.ok(ApiResponse.success(nextFireTimes, "다음 실행 시간 조회를 완료했습니다."));
    }

    @Operation(summary = "Quartz Job 생성", description = "새로운 Quartz Job을 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createJob(@Valid @RequestBody QuartzJobRequest request,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.createJob(request, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job 등록을 완료 했습니다."));
    }

    @Operation(summary = "Quartz Job 수정", description = "기존 Quartz Job의 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<String>> updateJob(@Valid @RequestBody QuartzJobRequest request,
                                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.updateJob(request, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job 수정을 완료 했습니다."));
    }

    @Operation(summary = "Quartz Job 삭제", description = "특정 Quartz Job을 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/delete/{metaId}")
    public ResponseEntity<ApiResponse<String>> deleteJob(@PathVariable Long metaId,
                                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.deleteJob(metaId, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job 삭제를 완료 했습니다."));
    }

    @Operation(summary = "Quartz Job 일시정지", description = "특정 Quartz Job을 일시정지합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일시정지 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/pause/{metaId}")
    public ResponseEntity<ApiResponse<String>> pauseJob(@PathVariable Long metaId,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.pauseJob(metaId, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("일시 정지 했습니다."));
    }

    @Operation(summary = "Quartz Job 재개", description = "일시정지된 Quartz Job을 재개합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재개 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/resume/{metaId}")
    public ResponseEntity<ApiResponse<String>> resumeJob(@PathVariable Long metaId,
                                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.resumeJob(metaId, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("다시 활성화되었습니다."));
    }

    @Operation(summary = "Quartz Job 즉시 실행", description = "특정 Quartz Job을 즉시 실행합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "즉시 실행 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/trigger/{metaId}")
    public ResponseEntity<ApiResponse<String>> triggerJobNow(@PathVariable Long metaId,
                                                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.triggerJobNow(metaId, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("즉시 수행을 완료 했습니다."));
    }
}
