package com.quartzbatchcontrol.dashboard.api;

import com.quartzbatchcontrol.dashboard.api.response.BatchCountResponse;
import com.quartzbatchcontrol.dashboard.api.response.DailyStatusCountResponse;
import com.quartzbatchcontrol.dashboard.api.response.QuartzCountResponse;
import com.quartzbatchcontrol.dashboard.application.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/batchCount")
    public BatchCountResponse getBatchCount() {
        return dashboardService.getBatchCount();
    }

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
