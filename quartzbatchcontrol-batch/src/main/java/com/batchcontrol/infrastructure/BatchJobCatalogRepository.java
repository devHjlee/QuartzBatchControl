package com.batchcontrol.infrastructure;

import com.batchcontrol.domain.BatchJobCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchJobCatalogRepository extends JpaRepository<BatchJobCatalog, Long> {
    List<BatchJobCatalog> findAllByDeletedFalse();
}
