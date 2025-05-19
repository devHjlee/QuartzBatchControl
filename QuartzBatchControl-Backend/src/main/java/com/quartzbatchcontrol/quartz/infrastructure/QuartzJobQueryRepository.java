package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.quartz.api.response.QuartzJobList;
import com.quartzbatchcontrol.quartz.domain.QQuartzCronTriggerView;

import com.quartzbatchcontrol.quartz.domain.QQuartzJobMeta;
import com.quartzbatchcontrol.quartz.domain.QQuartzTriggerView;
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
        QQuartzTriggerView trigger = QQuartzTriggerView.quartzTriggerView;
        QQuartzCronTriggerView cron = QQuartzCronTriggerView.quartzCronTriggerView;
        QQuartzJobMeta meta = QQuartzJobMeta.quartzJobMeta;

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
                        meta.createdBy
                ))
                .from(trigger)
                .leftJoin(cron).on(
                        trigger.triggerName.eq(cron.triggerName),
                        trigger.triggerGroup.eq(cron.triggerGroup)
                )
                .leftJoin(meta).on(
                        meta.jobName.eq(trigger.jobName),
                        meta.jobGroup.eq(trigger.jobGroup),
                        meta.eventType.eq(QuartzJobEventType.REGISTER),
                        meta.createdAt.eq(
                                select(meta.createdAt.max())
                                        .from(meta)
                                        .where(
                                                meta.jobName.eq(trigger.jobName),
                                                meta.jobGroup.eq(trigger.jobGroup),
                                                meta.eventType.eq(QuartzJobEventType.REGISTER)
                                        )
                        )
                )
                .fetch();
    }
}
