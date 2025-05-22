package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.batch.domain.QBatchJobMeta;
import com.quartzbatchcontrol.quartz.api.response.QuartzJobList;
import com.quartzbatchcontrol.quartz.domain.*;
import com.quartzbatchcontrol.quartz.enums.QuartzJobEventType;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;

@Repository
@RequiredArgsConstructor
public class QuartzJobQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<QuartzJobList> findQuartzJobs() {
        QQuartzJobDetailView detail = QQuartzJobDetailView.quartzJobDetailView;
        QQuartzTriggerView trigger = QQuartzTriggerView.quartzTriggerView;
        QQuartzCronTriggerView cron = QQuartzCronTriggerView.quartzCronTriggerView;
        QQuartzJobMeta meta = QQuartzJobMeta.quartzJobMeta;
        QBatchJobMeta batchMeta = QBatchJobMeta.batchJobMeta;

        return queryFactory
                .select(Projections.constructor(
                        QuartzJobList.class,
                        trigger.jobName,
                        trigger.jobGroup,
                        trigger.triggerState,
                        cron.cronExpression,
                        trigger.nextFireTime,
                        trigger.previousFireTime,
                        meta.jobType,
                        meta.eventType,
                        meta.metaId,
                        meta.createdBy
                ))
                .from(meta)
                .leftJoin(detail).on(
                        meta.jobGroup.eq(detail.jobGroup),
                        meta.jobName.eq(detail.jobName)
                )
                .leftJoin(batchMeta).on(
                        meta.metaId.eq(batchMeta.id)
                )
                .leftJoin(trigger).on(
                        meta.jobGroup.eq(trigger.jobGroup),
                        meta.jobName.eq(trigger.jobName)
                )
                .leftJoin(cron).on(
                        trigger.triggerName.eq(cron.triggerName),
                        trigger.triggerGroup.eq(cron.triggerGroup)
                )
                .fetch();
    }
}
