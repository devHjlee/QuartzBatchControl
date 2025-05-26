package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.quartz.api.response.QuartzJobMetaSummaryResponse;
import com.quartzbatchcontrol.quartz.domain.QQuartzCronTriggerView;
import com.quartzbatchcontrol.quartz.domain.QQuartzJobDetailView;
import com.quartzbatchcontrol.quartz.domain.QQuartzJobMeta;
import com.quartzbatchcontrol.quartz.domain.QQuartzTriggerView;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuartzJobMetaRepositoryImpl implements QuartzJobMetaRepositoryCustom{
    private final JPAQueryFactory jpaQueryFactory;
    private final QQuartzJobMeta quartzJobMeta = QQuartzJobMeta.quartzJobMeta;
    private final QQuartzJobDetailView quartzJobDetailView = QQuartzJobDetailView.quartzJobDetailView;
    private final QQuartzTriggerView quartzTriggerView = QQuartzTriggerView.quartzTriggerView;
    private final QQuartzCronTriggerView quartzCronTriggerView = QQuartzCronTriggerView.quartzCronTriggerView;

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
                )
                .leftJoin(quartzCronTriggerView).on(
                        quartzTriggerView.triggerName.eq(quartzCronTriggerView.triggerName)
                                .and(quartzTriggerView.triggerGroup.eq(quartzCronTriggerView.triggerGroup))
                )
                .orderBy(quartzJobMeta.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = (long) content.size();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
