package com.quartzbatchcontrol.batch.infrastructure;

import com.quartzbatchcontrol.batch.domain.BatchJobCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BatchJobCatalogRepository extends JpaRepository<BatchJobCatalog, Long> {
    @Query("SELECT b.jobName FROM BatchJobCatalog b WHERE b.deleted = false")
    List<String> findJobNameByDeletedFalse();
    Optional<BatchJobCatalog> findBatchJobCatalogByJobNameAndDeletedFalse(String jobName);
}
