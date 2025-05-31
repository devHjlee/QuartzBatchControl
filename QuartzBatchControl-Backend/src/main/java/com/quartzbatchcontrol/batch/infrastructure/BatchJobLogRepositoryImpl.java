package com.quartzbatchcontrol.batch.infrastructure;

import com.quartzbatchcontrol.batch.api.request.BatchLogSearchRequest;
import com.quartzbatchcontrol.batch.api.response.BatchLogResponse;
import com.quartzbatchcontrol.batch.domain.QBatchJobLog;
import com.quartzbatchcontrol.batch.domain.QBatchJobMeta;
import com.quartzbatchcontrol.dashboard.api.response.DailyStatusCountResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BatchJobLogRepositoryImpl implements BatchJobLogRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private final QBatchJobLog batchJobLog = QBatchJobLog.batchJobLog;
    private final QBatchJobMeta batchJobMeta = QBatchJobMeta.batchJobMeta;

    @Override
    public Page<BatchLogResponse> findBySearchLog(BatchLogSearchRequest request, Pageable pageable) {
        List<BatchLogResponse> content = queryFactory
                .select(Projections.constructor(
                        BatchLogResponse.class,
                        batchJobLog.id,
                        batchJobLog.jobExecutionId,
                        batchJobLog.jobName,
                        batchJobMeta.id,
                        batchJobMeta.metaName,
                        batchJobLog.startTime,
                        batchJobLog.endTime,
                        batchJobLog.status,
                        batchJobLog.exitCode,
                        batchJobLog.exitMessage,
                        batchJobLog.jobParameters
                ))
                .from(batchJobLog)
                .leftJoin(batchJobMeta).on(
                        batchJobLog.metaId.eq(batchJobMeta.id)
                                .and(batchJobLog.jobName.eq(batchJobMeta.jobName))
                )
                .where(
                        keywordContains(request.getKeyword()),
                        statusContains(request.getStatus())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(batchJobLog.startTime.desc())
                .fetch();

        Long total = queryFactory
                .select(batchJobLog.count())
                .from(batchJobLog)
                .leftJoin(batchJobMeta).on(
                        batchJobLog.metaId.eq(batchJobMeta.id)
                                .and(batchJobLog.jobName.eq(batchJobMeta.jobName))
                )
                .where(
                        keywordContains(request.getKeyword()),
                        statusContains(request.getStatus())
                )
                .fetchOne();

        long totalCount = (total == null) ? 0L : total;

        return new PageImpl<>(content, pageable, totalCount);
    }

    @Override
    public DailyStatusCountResponse findBatchLogCount() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        return queryFactory
                .select(Projections.constructor(
                        DailyStatusCountResponse.class,
                        new CaseBuilder()
                                .when(batchJobLog.status.eq("COMPLETED"))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(batchJobLog.status.eq("FAILED"))
                                .then(1L)
                                .otherwise(0L)
                                .sum()
                    ))
                .from(batchJobLog)
                .where(
                        batchJobLog.startTime.goe(startOfToday)
                                .and(batchJobLog.startTime.lt(startOfTomorrow))
                )
                .fetchOne();
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        return batchJobLog.jobName.containsIgnoreCase(keyword)
                .or(batchJobMeta.metaName.containsIgnoreCase(keyword));
    }

    private BooleanExpression statusContains(String status) {
        return StringUtils.hasText(status) ? batchJobLog.status.containsIgnoreCase(status) : null;
    }
}
