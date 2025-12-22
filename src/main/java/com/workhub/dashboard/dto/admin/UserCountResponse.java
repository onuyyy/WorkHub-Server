package com.workhub.dashboard.dto.admin;

public record UserCountResponse(
        Long totalUserCount
) {
    public static UserCountResponse from(Long count) {
        return new UserCountResponse(count);
    }
}
