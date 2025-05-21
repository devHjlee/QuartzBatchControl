package com.quartzbatchcontrol.batch.api.response;

import com.quartzbatchcontrol.batch.domain.BatchJobMeta;
import lombok.Builder;
import lombok.Getter;
// import lombok.extern.slf4j.Slf4j; // 로그 사용 안함

// import java.io.IOException; // IOException 사용 안함


@Getter
public class BatchJobParameterResponse {
    private final Long id;
    private final String jobParameters; // 타입을 String으로 변경


    @Builder
    public BatchJobParameterResponse(Long id, String jobParameters) {
        this.id = id;
        this.jobParameters = jobParameters; // String 타입으로 받음
    }

    public static BatchJobParameterResponse from(BatchJobMeta entity) { // ObjectMapper 파라미터 제거
        // Map<String, Object> params = Collections.emptyMap(); // 파싱 로직 제거
        // if (entity.getJobParameters() != null && !entity.getJobParameters().isBlank()) {
        // try {
        // params = objectMapper.readValue(entity.getJobParameters(), new TypeReference<Map<String, Object>>() {});
        // } catch (IOException e) {
        // log.error("Failed to parse jobParameters for metaId {}: {}", entity.getId(), e.getMessage());
        // }
        // }
        boolean isScheduled = false; // Quartz 연동 전까지 false

        return BatchJobParameterResponse.builder()
                .id(entity.getId())
                .jobParameters(entity.getJobParameters()) // 엔티티의 문자열 그대로 전달
                .build();
    }
} 