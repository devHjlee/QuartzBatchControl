package com.quartzbatchcontrol.quartz.api;

import com.quartzbatchcontrol.global.response.ApiResponse;
import com.quartzbatchcontrol.quartz.api.request.QuartzLogSearchRequest;
import com.quartzbatchcontrol.quartz.api.response.QuartzLogResponse;
import com.quartzbatchcontrol.quartz.application.QuartzJobExecutionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quartz-log")
@RequiredArgsConstructor
public class QuartzLogController {
    private final QuartzJobExecutionLogService quartzJobExecutionLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<QuartzLogResponse>>> getAllQuartzLogs(
            @ModelAttribute QuartzLogSearchRequest request,
            Pageable pageable) {
        Page<QuartzLogResponse> contents = quartzJobExecutionLogService.getAllQuartzLogs(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(new PagedModel<>(contents), "Quartz 로그 조회를 완료했습니다."));
    }
}
