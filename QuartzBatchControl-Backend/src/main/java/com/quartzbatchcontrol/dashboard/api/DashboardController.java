package com.quartzbatchcontrol.dashboard.api;

import com.quartzbatchcontrol.dashboard.api.response.BatchCountResponse;
import com.quartzbatchcontrol.dashboard.api.response.DailyStatusCountResponse;
import com.quartzbatchcontrol.dashboard.api.response.QuartzCountResponse;
import com.quartzbatchcontrol.dashboard.application.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "대시보드 관련 통계 API")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @Operation(summary = "Batch Job 조회", description = "Batch Job 통계를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BatchCountResponse.class)))
    })
    @GetMapping("/batchCount")
    public BatchCountResponse getBatchCount() {
        return dashboardService.getBatchCount();
    }

    @Operation(summary = "Batch Log 조회", description = "Batch Log 통계를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BatchCountResponse.class)))
    })
    @GetMapping("/batchLogCount")
    public DailyStatusCountResponse getBatchLogCount() {
        return dashboardService.getBatchLogCount();
    }

    @GetMapping("/quartzCount")
    public QuartzCountResponse getQuartzCount() {
        return dashboardService.getQuartzCount();
    }

    @GetMapping("/quartzLogCount")
    public DailyStatusCountResponse getQuartzLogCount() {
        return dashboardService.getQuartzLogCount();
    }
}
