package com.quartzbatchcontrol.quartz.infrastructure;

import com.quartzbatchcontrol.dashboard.api.response.DailyStatusCountResponse;
import com.quartzbatchcontrol.quartz.api.request.QuartzLogSearchRequest;
import com.quartzbatchcontrol.quartz.api.response.QuartzLogResponse;
import com.quartzbatchcontrol.quartz.domain.QQuartzJobExecutionLog; // QueryDSL Q-Type
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
public class QuartzJobExecutionLogRepositoryImpl implements QuartzJobExecutionLogRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QQuartzJobExecutionLog quartzLog = QQuartzJobExecutionLog.quartzJobExecutionLog;

    @Override
    public Page<QuartzLogResponse> findBySearchLog(QuartzLogSearchRequest request, Pageable pageable) {
        List<QuartzLogResponse> content = queryFactory
                .select(Projections.constructor(QuartzLogResponse.class,
                        quartzLog.id,
                        quartzLog.jobName,
                        quartzLog.jobGroup,
                        quartzLog.startTime,
                        quartzLog.endTime,
                        quartzLog.status,
                        quartzLog.message
                ))
                .from(quartzLog)
                .where(
                        jobNameContains(request.getKeyword()),
                        jobGroupEquals(request.getJobGroup()),
                        statusEquals(request.getStatus())
                        // TODO: 기간 검색 조건 추가 시 여기에 메소드 호출 추가
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(quartzLog.startTime.desc()) // 기본 정렬 (Pageable의 sort로 대체 가능)
                .fetch();

        // 전체 카운트 조회 (페이징 처리를 위해)
        Long total = queryFactory
                .select(quartzLog.count())
                .from(quartzLog)
                .where(
                        jobNameContains(request.getKeyword()),
                        jobGroupEquals(request.getJobGroup()),
                        statusEquals(request.getStatus())
                )
                .fetchOne();
        
        long totalCount = (total == null) ? 0L : total;

        return new PageImpl<>(content, pageable, totalCount);
    }

    @Override
    public DailyStatusCountResponse findQuartzStateCount() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        return queryFactory
                .select(Projections.constructor(
                        DailyStatusCountResponse.class,
                        new CaseBuilder()
                                .when(quartzLog.status.eq("SUCCESS"))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(quartzLog.status.eq("FAIL"))
                                .then(1L)
                                .otherwise(0L)
                                .sum()
                ))
                .from(quartzLog)
                .where(
                        quartzLog.startTime.goe(startOfToday)
                                .and(quartzLog.startTime.lt(startOfTomorrow))
                )
                .fetchOne();
    }

    private BooleanExpression jobNameContains(String jobName) {
        return StringUtils.hasText(jobName) ? quartzLog.jobName.containsIgnoreCase(jobName) : null;
    }

    private BooleanExpression jobGroupEquals(String jobGroup) {
        return StringUtils.hasText(jobGroup) ? quartzLog.jobGroup.equalsIgnoreCase(jobGroup) : null;
    }

    private BooleanExpression statusEquals(String status) {
        return StringUtils.hasText(status) ? quartzLog.status.equalsIgnoreCase(status) : null;
    }
    
    // TODO: 기간 검색을 위한 BooleanExpression 메소드 추가 예시
    /*
    private BooleanExpression startTimeGoe(LocalDateTime startDate) {
        return startDate != null ? quartzLog.startTime.goe(startDate) : null;
    }

    private BooleanExpression endTimeLoe(LocalDateTime endDate) {
        return endDate != null ? quartzLog.endTime.loe(endDate) : null;
    }
    */
} 