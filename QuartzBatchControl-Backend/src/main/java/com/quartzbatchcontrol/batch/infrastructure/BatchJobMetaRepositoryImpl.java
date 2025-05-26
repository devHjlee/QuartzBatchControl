package com.quartzbatchcontrol.batch.infrastructure;

import com.quartzbatchcontrol.batch.api.response.BatchJobMetaSummaryResponse;
import com.quartzbatchcontrol.batch.domain.QBatchJobMeta;
import com.quartzbatchcontrol.quartz.domain.QQuartzJobMeta;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BatchJobMetaRepositoryImpl implements BatchJobMetaRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QBatchJobMeta batchJobMeta = QBatchJobMeta.batchJobMeta;
    private final QQuartzJobMeta quartzJobMeta = QQuartzJobMeta.quartzJobMeta;

    @Override
    public Page<BatchJobMetaSummaryResponse> findBySearchCondition(String jobName, String metaName, Pageable pageable) {
        List<BatchJobMetaSummaryResponse> content = queryFactory
                .select(Projections.constructor(
                        BatchJobMetaSummaryResponse.class,
                        batchJobMeta.id,
                        batchJobMeta.jobName,
                        batchJobMeta.metaName,
                        batchJobMeta.jobDescription,
                        batchJobMeta.jobParameters,
                        Expressions.constant(0),
                        batchJobMeta.createdBy,
                        batchJobMeta.createdAt,
                        batchJobMeta.updatedBy,
                        batchJobMeta.updatedAt,
                        quartzJobMeta.id.isNotNull()
                ))
                .from(batchJobMeta)
                .leftJoin(quartzJobMeta).on(
                        batchJobMeta.id.eq(quartzJobMeta.batchMetaId)
                )
                .where(
                        jobNameContains(jobName),
                        metaNameContains(metaName)
                )
                .orderBy(batchJobMeta.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(batchJobMeta.count())
                .from(batchJobMeta)
                .where(
                        jobNameContains(jobName),
                        metaNameContains(metaName)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression jobNameContains(String jobName) {
        return StringUtils.hasText(jobName) ? batchJobMeta.jobName.containsIgnoreCase(jobName) : null;
    }

    private BooleanExpression metaNameContains(String metaName) {
        return StringUtils.hasText(metaName) ? batchJobMeta.metaName.containsIgnoreCase(metaName) : null;
    }
} 