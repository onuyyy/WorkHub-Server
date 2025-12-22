package com.workhub.dashboard.dto.admin;

public record MonthlyMetricsMetadata(
        String usersCriteria,
        String projectsCriteria,
        int months,
        String startMonth,
        String endMonth
) {
    public static MonthlyMetricsMetadata of(String usersCriteria, String projectsCriteria,
                                            int months, String startMonth, String endMonth) {
        return new MonthlyMetricsMetadata(usersCriteria, projectsCriteria, months, startMonth, endMonth);
    }
}
