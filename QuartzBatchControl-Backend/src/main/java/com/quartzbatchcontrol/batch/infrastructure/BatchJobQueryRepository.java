package com.quartzbatchcontrol.batch.infrastructure;

import com.quartzbatchcontrol.batch.api.response.BatchJobListResponse;
import com.quartzbatchcontrol.batch.domain.QBatchJobExecutionView;
import com.quartzbatchcontrol.batch.domain.QBatchJobInstanceView;
import com.quartzbatchcontrol.batch.domain.QBatchJobMeta;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;

@Repository
@RequiredArgsConstructor
public class BatchJobQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<BatchJobListResponse> findBatchJobsWithSummary() {
        QBatchJobMeta meta = QBatchJobMeta.batchJobMeta;
        QBatchJobInstanceView instance = QBatchJobInstanceView.batchJobInstanceView;
        QBatchJobExecutionView exec = QBatchJobExecutionView.batchJobExecutionView;

        // latest 실행용 alias
        QBatchJobExecutionView exec1 = new QBatchJobExecutionView("exec1");
        QBatchJobInstanceView inst1 = new QBatchJobInstanceView("inst1");

        // stats용 alias
        QBatchJobExecutionView exec2 = new QBatchJobExecutionView("exec2");
        QBatchJobInstanceView inst2 = new QBatchJobInstanceView("inst2");

        return queryFactory
                .select(Projections.constructor(
                        BatchJobListResponse.class,

                        // 메타
                        meta.id,
                        meta.jobName,
                        meta.jobDescription,
                        meta.defaultParams,

                        // 최신 실행
                        exec.startTime,
                        exec.status,
                        exec.exitMessage,

                        // 실행 통계: 전체 count
                        JPAExpressions.select(exec2.id.count())
                                .from(exec2)
                                .join(inst2).on(exec2.jobInstanceId.eq(inst2.id))
                                .where(inst2.jobName.eq(meta.jobName)),
                        // 실행 통계: 성공 count
                        JPAExpressions.select(
                                        new CaseBuilder()
                                                .when(exec2.status.eq("COMPLETED")).then(1L)
                                                .otherwise(0L)
                                                .sum()
                                )
                                .from(exec2)
                                .join(inst2).on(exec2.jobInstanceId.eq(inst2.id))
                                .where(inst2.jobName.eq(meta.jobName)),
                        // 실행 통계: 실패 count
                        JPAExpressions.select(
                                        new CaseBuilder()
                                                .when(exec2.status.eq("FAILED")).then(1L)
                                                .otherwise(0L)
                                                .sum()
                                )
                                .from(exec2)
                                .join(inst2).on(exec2.jobInstanceId.eq(inst2.id))
                                .where(inst2.jobName.eq(meta.jobName)),

                        // 메타 관리 정보
                        meta.createdAt,
                        meta.createdBy,
                        meta.updatedAt,
                        meta.updatedBy
                ))
                .from(meta)
                .join(instance).on(meta.jobName.eq(instance.jobName))
                .join(exec).on(exec.jobInstanceId.eq(instance.id))

                // 최신 실행만 조회: exec.startTime = (subquery max startTime)
                .where(exec.startTime.eq(
                        JPAExpressions.select(exec1.startTime.max())
                                .from(exec1)
                                .join(inst1).on(exec1.jobInstanceId.eq(inst1.id))
                                .where(inst1.jobName.eq(meta.jobName))
                ))

                .fetch();
    }



}
