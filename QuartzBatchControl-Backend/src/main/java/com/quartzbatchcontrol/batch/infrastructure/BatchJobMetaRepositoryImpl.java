package com.quartzbatchcontrol.batch.infrastructure;

import com.quartzbatchcontrol.batch.api.response.BatchJobMetaSummaryResponse;
import com.quartzbatchcontrol.batch.domain.QBatchJobMeta;
import com.quartzbatchcontrol.dashboard.api.response.BatchCountResponse;
import com.quartzbatchcontrol.quartz.domain.QQuartzJobMeta;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BatchJobMetaRepositoryImpl implements BatchJobMetaRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QBatchJobMeta batchJobMeta = QBatchJobMeta.batchJobMeta;
    private final QQuartzJobMeta quartzJobMeta = QQuartzJobMeta.quartzJobMeta;

    @Override
    public Page<BatchJobMetaSummaryResponse> findBySearchCondition(String keyword, Pageable pageable) {
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
                        keywordContains(keyword)
                )
                .orderBy(batchJobMeta.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(batchJobMeta.count())
                .from(batchJobMeta)
                .where(
                        keywordContains(keyword)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public BatchCountResponse findBatchCount() {
        return queryFactory
                .select(Projections.constructor(
                        BatchCountResponse.class,
                        batchJobMeta.count(),
                        new CaseBuilder()
                                .when(quartzJobMeta.batchMetaId.isNotNull())
                                .then(1L)
                                .otherwise(0L)
                                .sum()
                ))
                .from(batchJobMeta)
                .leftJoin(quartzJobMeta).on(
                        batchJobMeta.id.eq(quartzJobMeta.batchMetaId)
                )
                .fetchOne();
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        return batchJobMeta.jobName.containsIgnoreCase(keyword)
                .or(batchJobMeta.metaName.containsIgnoreCase(keyword));
    }
} 