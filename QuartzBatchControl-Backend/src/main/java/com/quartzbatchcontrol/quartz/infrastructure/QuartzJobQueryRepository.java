package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.quartz.api.response.QuartzJobList;
import com.quartzbatchcontrol.quartz.domain.QQuartzCronTriggerView;
import com.quartzbatchcontrol.quartz.domain.QQuartzJobHistory;
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
        QQuartzJobHistory history = QQuartzJobHistory.quartzJobHistory;

        return queryFactory
                .select(Projections.constructor(
                        QuartzJobList.class,
                        trigger.jobName,
                        trigger.jobGroup,
                        trigger.triggerState,
                        cron.cronExpression,
                        trigger.nextFireTime,
                        trigger.previousFireTime,
                        history.jobType,
                        history.eventType,
                        history.createdBy
                ))
                .from(trigger)
                .leftJoin(cron).on(
                        trigger.triggerName.eq(cron.triggerName),
                        trigger.triggerGroup.eq(cron.triggerGroup)
                )
                .leftJoin(history).on(
                        history.jobName.eq(trigger.jobName),
                        history.jobGroup.eq(trigger.jobGroup),
                        history.eventType.eq(QuartzJobEventType.REGISTER),
                        history.createdAt.eq(
                                select(history.createdAt.max())
                                        .from(history)
                                        .where(
                                                history.jobName.eq(trigger.jobName),
                                                history.jobGroup.eq(trigger.jobGroup),
                                                history.eventType.eq(QuartzJobEventType.REGISTER)
                                        )
                        )
                )
                .fetch();
    }
}
