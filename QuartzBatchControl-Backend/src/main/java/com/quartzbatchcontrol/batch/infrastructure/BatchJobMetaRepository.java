package com.quartzbatchcontrol.batch.infrastructure;

import com.quartzbatchcontrol.batch.domain.BatchJobMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchJobMetaRepository extends JpaRepository<BatchJobMeta, Long> {
    List<BatchJobMeta> findByMetaName(String metaName);
    boolean existsByJobNameAndMetaName(String jobName, String metaName);
}
