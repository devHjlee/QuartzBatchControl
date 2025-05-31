package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.batch.domain.QBatchJobMeta;
import com.quartzbatchcontrol.dashboard.api.response.QuartzCountResponse;
import com.quartzbatchcontrol.quartz.api.response.QuartzJobMetaSummaryResponse;
import com.quartzbatchcontrol.quartz.domain.QQuartzCronTriggerView;
import com.quartzbatchcontrol.quartz.domain.QQuartzJobDetailView;
import com.quartzbatchcontrol.quartz.domain.QQuartzJobMeta;
import com.quartzbatchcontrol.quartz.domain.QQuartzTriggerView;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuartzJobMetaRepositoryImpl implements QuartzJobMetaRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    private final QQuartzJobMeta quartzJobMeta = QQuartzJobMeta.quartzJobMeta;
    private final QQuartzJobDetailView quartzJobDetailView = QQuartzJobDetailView.quartzJobDetailView;
    private final QQuartzTriggerView quartzTriggerView = QQuartzTriggerView.quartzTriggerView;
    private final QQuartzCronTriggerView quartzCronTriggerView = QQuartzCronTriggerView.quartzCronTriggerView;
    private final QBatchJobMeta batchJobMeta = QBatchJobMeta.batchJobMeta;

    @Override
    public Page<QuartzJobMetaSummaryResponse> findBySearchCondition(String keyword, Pageable pageable) {
        List<QuartzJobMetaSummaryResponse> content = jpaQueryFactory
                .select(Projections.constructor(
                        QuartzJobMetaSummaryResponse.class,
                        quartzJobMeta.id,
                        quartzJobMeta.jobName,
                        quartzJobMeta.jobGroup,
                        quartzJobMeta.jobType,
                        quartzJobMeta.batchMetaId,
                        quartzTriggerView.triggerName,
                        quartzTriggerView.triggerGroup,
                        quartzTriggerView.nextFireTime,
                        quartzTriggerView.previousFireTime,
                        quartzTriggerView.triggerState,
                        quartzCronTriggerView.cronExpression,
                        batchJobMeta.metaName,
                        quartzJobMeta.createdBy
                ))
                .from(quartzJobMeta)
                .leftJoin(quartzJobDetailView).on(
                        quartzJobMeta.jobName.eq(quartzJobDetailView.jobName)
                                .and(quartzJobMeta.jobGroup.eq(quartzJobDetailView.jobGroup))
                )
                .leftJoin(quartzTriggerView).on(
                        quartzJobDetailView.jobName.eq(quartzTriggerView.jobName)
                                .and(quartzJobDetailView.jobGroup.eq(quartzTriggerView.jobGroup))
                                .and(quartzJobDetailView.jobGroup.eq(quartzTriggerView.triggerGroup))
                )
                .leftJoin(quartzCronTriggerView).on(
                        quartzTriggerView.triggerName.eq(quartzCronTriggerView.triggerName)
                                .and(quartzTriggerView.triggerGroup.eq(quartzCronTriggerView.triggerGroup))
                )
                .leftJoin(batchJobMeta).on(
                        quartzJobMeta.batchMetaId.eq(batchJobMeta.id)
                )
                .where(containsKeyword(keyword))
                .orderBy(quartzJobMeta.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(quartzJobMeta.count())
                .from(quartzJobMeta)
                .leftJoin(quartzJobDetailView).on(
                        quartzJobMeta.jobName.eq(quartzJobDetailView.jobName)
                                .and(quartzJobMeta.jobGroup.eq(quartzJobDetailView.jobGroup))
                )
                .leftJoin(quartzTriggerView).on(
                        quartzJobDetailView.jobName.eq(quartzTriggerView.jobName)
                                .and(quartzJobDetailView.jobGroup.eq(quartzTriggerView.jobGroup))
                                .and(quartzJobDetailView.jobGroup.eq(quartzTriggerView.triggerGroup))
                )
                .leftJoin(quartzCronTriggerView).on(
                        quartzTriggerView.triggerName.eq(quartzCronTriggerView.triggerName)
                                .and(quartzTriggerView.triggerGroup.eq(quartzCronTriggerView.triggerGroup))
                )
                .leftJoin(batchJobMeta).on(
                        quartzJobMeta.batchMetaId.eq(batchJobMeta.id)
                )
                .where(containsKeyword(keyword))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public QuartzCountResponse findQuartzJobCount() {
        return jpaQueryFactory
                .select(Projections.constructor(
                        QuartzCountResponse.class,
                        quartzTriggerView.count(),
                        new CaseBuilder()
                                .when(quartzTriggerView.triggerState.eq("WAITING"))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(quartzTriggerView.triggerState.eq("ACQUIRED"))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(quartzTriggerView.triggerState.eq("PAUSED"))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(quartzTriggerView.triggerState.eq("BLOCKED"))
                                .then(1L)
                                .otherwise(0L)
                                .sum()
                ))
                .from(quartzTriggerView)
                .fetchOne();
    }

    private BooleanExpression containsKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        return quartzJobMeta.jobName.containsIgnoreCase(keyword)
                .or(batchJobMeta.metaName.containsIgnoreCase(keyword));
    }
}
