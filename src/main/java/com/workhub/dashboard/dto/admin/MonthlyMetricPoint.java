package com.workhub.dashboard.dto.admin;

public record MonthlyMetricPoint(
        String month,
        Long value
) {
    public static MonthlyMetricPoint of(String month, Long value) {
        return new MonthlyMetricPoint(month, value);
    }
}
