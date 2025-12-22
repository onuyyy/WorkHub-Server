package com.workhub.dashboard.service.admin;

import com.workhub.dashboard.dto.admin.CompanyCountResponse;
import com.workhub.dashboard.dto.admin.MonthlyMetricPoint;
import com.workhub.dashboard.dto.admin.MonthlyMetricsMetadata;
import com.workhub.dashboard.dto.admin.MonthlyMetricsResponse;
import com.workhub.dashboard.dto.admin.ProjectCountResponse;
import com.workhub.dashboard.dto.admin.UserCountResponse;
import com.workhub.project.service.ProjectService;
import com.workhub.userTable.service.CompanyService;
import com.workhub.userTable.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashBoardAdminService {

    private static final int DEFAULT_MONTH_RANGE = 12;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final String USER_CRITERIA = "월 말일 기준 deleted_at null 또는 말일 이후";
    private static final String PROJECT_CRITERIA = "계약 기간이 월과 겹치는 프로젝트";

    private final UserService userService;
    private final CompanyService companyService;
    private final ProjectService projectService;

    /**
     * 활성 사용자 수를 조회한다.
     */
    public UserCountResponse getUserCount() {

        return UserCountResponse.from(userService.countActiveUsers());
    }

    /**
     * 운영 중인 회사 수를 조회한다.
     */
    public CompanyCountResponse getCompanyCount() {

        return CompanyCountResponse.from(companyService.countActiveCompanies());
    }

    /**
     * 진행/완료 상태의 프로젝트 수를 조회한다.
     */
    public ProjectCountResponse getProjectCount() {

        return ProjectCountResponse.from(projectService.countInProgressOrCompletedProjects());
    }

    /**
     * 최근 N개월 간 월별 사용자·프로젝트 지표를 계산한다.
     */
    public MonthlyMetricsResponse getMonthlyMetrics(Integer months) {
        int monthRange = normalizeMonthRange(months);

        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(monthRange - 1L);

        List<YearMonth> monthSeries = buildMonthSeries(startMonth, endMonth);

        List<MonthlyMetricPoint> userMetrics = new ArrayList<>();
        List<MonthlyMetricPoint> projectMetrics = new ArrayList<>();

        for (YearMonth month : monthSeries) {
            LocalDate monthStart = month.atDay(1);
            LocalDate monthEnd = month.atEndOfMonth();

            Long userCount = userService.countActiveUsersUntil(monthEnd.atTime(LocalTime.MAX));
            Long projectCount = projectService.countProjectsOverlapping(monthStart, monthEnd);

            String formattedMonth = formatMonth(month);
            userMetrics.add(MonthlyMetricPoint.of(formattedMonth, userCount));
            projectMetrics.add(MonthlyMetricPoint.of(formattedMonth, projectCount));
        }

        MonthlyMetricsMetadata metadata = MonthlyMetricsMetadata.of(
                USER_CRITERIA,
                PROJECT_CRITERIA,
                monthRange,
                formatMonth(startMonth),
                formatMonth(endMonth)
        );

        return MonthlyMetricsResponse.of(userMetrics, projectMetrics, metadata);
    }

    /**
     * 유효한 월 범위를 설정한다.
     */
    private int normalizeMonthRange(Integer months) {
        if (months == null || months <= 0) {
            return DEFAULT_MONTH_RANGE;
        }
        return months;
    }

    /**
     * 시작~종료 월을 포함한 YearMonth 리스트를 생성한다.
     */
    private List<YearMonth> buildMonthSeries(YearMonth start, YearMonth end) {
        List<YearMonth> months = new ArrayList<>();
        YearMonth cursor = start;
        while (!cursor.isAfter(end)) {
            months.add(cursor);
            cursor = cursor.plusMonths(1);
        }
        return months;
    }

    /**
     * YearMonth를 ISO yyyy-MM 문자열로 변환한다.
     */
    private String formatMonth(YearMonth month) {
        return month.format(MONTH_FORMATTER);
    }
}
