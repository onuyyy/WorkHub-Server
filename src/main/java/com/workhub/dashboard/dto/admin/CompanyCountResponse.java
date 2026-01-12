package com.workhub.dashboard.dto.admin;

public record CompanyCountResponse(
        Long totalUserCount
) {
    public static CompanyCountResponse from(Long count) {
        return new CompanyCountResponse(count);
    }
}
