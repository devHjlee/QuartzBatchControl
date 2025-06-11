package com.batchcontrol.infrastructure;

import com.batchcontrol.domain.BatchJobMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobMetaRepository extends JpaRepository<BatchJobMeta, Long> {
}
