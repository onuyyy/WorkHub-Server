package com.workhub.dashboard.dto.admin;

public record ProjectCountResponse(
        Long totalProjectCount
) {
    public static ProjectCountResponse from(Long count) {
        return new ProjectCountResponse(count);
    }
}
