package com.workhub.dashboard.dto.admin;

import java.util.List;

public record MonthlyMetricsResponse(
        List<MonthlyMetricPoint> users,
        List<MonthlyMetricPoint> projects,
        MonthlyMetricsMetadata metadata
) {
    public static MonthlyMetricsResponse of(List<MonthlyMetricPoint> users,
                                            List<MonthlyMetricPoint> projects,
                                            MonthlyMetricsMetadata metadata) {
        return new MonthlyMetricsResponse(users, projects, metadata);
    }
}
