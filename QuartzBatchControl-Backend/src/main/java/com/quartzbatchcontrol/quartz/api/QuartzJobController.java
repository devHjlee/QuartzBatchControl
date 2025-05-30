package com.quartzbatchcontrol.quartz.api;

import com.quartzbatchcontrol.global.response.ApiResponse;
import com.quartzbatchcontrol.global.security.UserPrincipal;
import com.quartzbatchcontrol.quartz.api.request.QuartzJobRequest;
import com.quartzbatchcontrol.quartz.api.response.QuartzJobMetaSummaryResponse;
import com.quartzbatchcontrol.quartz.application.QuartzJobMetaService;
import com.quartzbatchcontrol.quartz.application.QuartzJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quartz-jobs")
@RequiredArgsConstructor
public class QuartzJobController {

    private final QuartzJobService quartzJobService;
    private final QuartzJobMetaService quartzJobMetaService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<QuartzJobMetaSummaryResponse>>> getAllQuartzJobMetas(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<QuartzJobMetaSummaryResponse> jobMetas = quartzJobMetaService.getAllQuartzJobMetas(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(new PagedModel<>(jobMetas), "배치 작업 메타데이터 목록 조회를 완료했습니다."));
    }

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

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createJob(@Valid @RequestBody QuartzJobRequest request,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.createJob(request, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job 등록을 완료 했습니다."));
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<String>> updateJob(@Valid @RequestBody QuartzJobRequest request,
                                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.updateJob(request, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job 수정을 완료 했습니다."));
    }

    @GetMapping("/delete/{metaId}")
    public ResponseEntity<ApiResponse<String>> deleteJob(@PathVariable Long metaId,
                                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.deleteJob(metaId, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job 삭제를 완료 했습니다."));
    }

    @GetMapping("/pause/{metaId}")
    public ResponseEntity<ApiResponse<String>> pauseJob(@PathVariable Long metaId,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.pauseJob(metaId, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("일시 정지 했습니다."));
    }

    @GetMapping("/resume/{metaId}")
    public ResponseEntity<ApiResponse<String>> resumeJob(@PathVariable Long metaId,
                                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.resumeJob(metaId, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("다시 활성화되었습니다."));
    }

    @GetMapping("/trigger/{metaId}")
    public ResponseEntity<ApiResponse<String>> triggerJobNow(@PathVariable Long metaId,
                                                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.triggerJobNow(metaId, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("즉시 수행을 완료 했습니다."));
    }
}
