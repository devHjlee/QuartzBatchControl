package com.batchcontrol.infrastructure;

import com.batchcontrol.domain.BatchJobCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobCatalogRepository extends JpaRepository<BatchJobCatalog, Long> {
}
