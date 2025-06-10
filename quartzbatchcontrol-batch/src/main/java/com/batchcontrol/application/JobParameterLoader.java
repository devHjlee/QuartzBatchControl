package com.batchcontrol.application;

import com.batchcontrol.domain.BatchJobMeta;
import com.batchcontrol.infrastructure.BatchJobMetaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
@Slf4j
@Component
@RequiredArgsConstructor
public class JobParameterLoader {

    private final BatchJobMetaRepository metaRepository;
    private final ObjectMapper objectMapper;

    public JobParameters buildJobParameters(JobParameters originalParams) throws JsonProcessingException {
        Long metaId = Long.parseLong(originalParams.getString("metaId"));

        BatchJobMeta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new RuntimeException("메타 정보 없음"));

        Map<String, Object> paramMap = objectMapper.readValue(
                meta.getJobParameters(), new TypeReference<>() {});

        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addLong("metaId", metaId);
        builder.addString("executedBy", originalParams.getString("executedBy"));

        // 저장된 파라미터를 반복문으로 설정
        paramMap.forEach((key, value) -> {
            if (value instanceof Integer) {
                builder.addLong(key, ((Integer) value).longValue());
            } else if (value instanceof Long) {
                builder.addLong(key, (Long) value);
            } else if (value instanceof Double) {
                builder.addDouble(key, (Double) value);
            } else {
                builder.addString(key, value.toString());
            }
        });

        return builder.toJobParameters();
    }
}
