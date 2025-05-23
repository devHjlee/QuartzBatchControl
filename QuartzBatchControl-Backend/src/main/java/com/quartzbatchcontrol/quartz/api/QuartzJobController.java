package com.quartzbatchcontrol.quartz.api;

import com.quartzbatchcontrol.global.response.ApiResponse;
import com.quartzbatchcontrol.global.security.UserPrincipal;
import com.quartzbatchcontrol.quartz.api.request.QuartzJobRequest;
import com.quartzbatchcontrol.quartz.application.QuartzJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/quartz-jobs")
@RequiredArgsConstructor
public class QuartzJobController {

    private final QuartzJobService quartzJobService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createJob(@Valid @RequestBody QuartzJobRequest request,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.createJob(request, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job 등록을 완료 했습니다."));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<String>> updateJob(@Valid @RequestBody QuartzJobRequest request,
                                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.updateJob(request, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job 수정을 완료 했습니다."));
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteJob(@Valid @RequestBody QuartzJobRequest request,
                                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.deleteJob(request, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job 삭제를 완료 했습니다."));
    }

    @PostMapping("/pause")
    public ResponseEntity<ApiResponse<String>> pauseJob(@Valid @RequestBody QuartzJobRequest request,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.pauseJob(request, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("일시 정지 했습니다."));
    }

    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<String>> resumeJob(@Valid @RequestBody QuartzJobRequest request,
                                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.resumeJob(request, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("다시 활성화되었습니다."));
    }

    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<String>> triggerJobNow(@Valid @RequestBody QuartzJobRequest request,
                                                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        quartzJobService.triggerJobNow(request, userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("즉시 수행을 완료 했습니다."));
    }
}
