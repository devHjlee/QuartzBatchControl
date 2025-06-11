package com.batchcontrol.runner;

import com.batchcontrol.domain.BatchJobCatalog;
import com.batchcontrol.infrastructure.BatchJobCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCatalogUpdater implements ApplicationRunner {

    private final ApplicationContext applicationContext;
    private final BatchJobCatalogRepository batchJobCatalogRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("배치 잡 카탈로그 동기화를 시작합니다...");

        // 1. 현재 코드에 존재하는 모든 잡의 이름을 가져옵니다.
        List<String> jobNames = applicationContext.getBeansOfType(Job.class).values()
                .stream()
                .map(Job::getName).toList();

        if (jobNames.isEmpty()) {
            log.warn("현재 코드에서 등록된 배치 잡을 찾을 수 없습니다.");
            // 모든 잡이 사라진 경우, 기존 카탈로그를 모두 삭제 처리할 수도 있습니다. (정책에 따라 결정)
        }

        // 2. DB에 저장된 모든 카탈로그를 조회합니다. (삭제되지 않은 것만)
        //    (성능을 위해 jobName을 key로 하는 Map으로 변환)
        Map<String, BatchJobCatalog> catalogsInDb = batchJobCatalogRepository.findAllByDeletedFalse()
                .stream()
                .collect(Collectors.toMap(BatchJobCatalog::getJobName, Function.identity()));

        // 3. 현재 코드를 기준으로 DB 카탈로그를 업데이트/추가합니다.
        for (String jobName : jobNames) {
            BatchJobCatalog catalog = catalogsInDb.get(jobName);

            if (catalog == null) {
                // DB에 없음 -> 새로운 잡이므로 추가
                log.info("새로운 잡을 카탈로그에 등록합니다: {}", jobName);
                BatchJobCatalog newCatalog = BatchJobCatalog.builder()
                        .jobName(jobName)
                        .deleted(false)
                        .build();
                batchJobCatalogRepository.save(newCatalog);
            } else {
                // DB에 존재함 -> 동기화 대상에서 제외 (이미 최신 상태)
                // 만약 설명 등을 업데이트하고 싶다면 여기서 로직 추가 가능
                catalogsInDb.remove(jobName);
            }
        }

        // 4. 순회가 끝난 후 'catalogsInDb' 맵에 남아있는 잡들은 코드에서 삭제된 것들입니다.
        if (!catalogsInDb.isEmpty()) {
            log.warn("코드에서 삭제된 것으로 보이는 잡을 비활성화 처리합니다: {}", catalogsInDb.keySet());
            for (BatchJobCatalog obsoleteCatalog : catalogsInDb.values()) {
                obsoleteCatalog.delete(); // 논리적 삭제 (deleted = true)
                batchJobCatalogRepository.save(obsoleteCatalog);
            }
        }

        log.info("배치 잡 카탈로그 동기화를 완료했습니다.");
    }
}
