package com.quartzbatchcontrol.quartz.api;

import com.quartzbatchcontrol.global.response.ApiResponse;
import com.quartzbatchcontrol.quartz.api.response.QuartzJobList;
import com.quartzbatchcontrol.quartz.application.QuartzJobQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quartz-jobs")
@RequiredArgsConstructor
public class QuartzJobQueryController {
    private final QuartzJobQueryService quartzJobQueryService;
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuartzJobList>>> getQuartzJobs(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String jobGroup
    ) {

        List<QuartzJobList> jobs = quartzJobQueryService.getJobs(jobName, jobGroup);
        return ResponseEntity.ok(ApiResponse.success(jobs,"조회 성공"));
    }
}
