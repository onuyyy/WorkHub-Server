package com.workhub.global.context;

/**
 * HTTP 요청 컨텍스트 정보를 담는 DTO
 * 히스토리 기록 시 요청자 정보(userId, userIp, userAgent)를 그룹화하여 전달
 */
public record RequestContext(
        Long userId,
        String userIp,
        String userAgent
) {
    /**
     * RequestContext 생성 팩토리 메서드
     *
     * @param userId 요청한 사용자 ID
     * @param userIp 요청자 IP 주소
     * @param userAgent 요청자 User-Agent
     * @return RequestContext 인스턴스
     */
    public static RequestContext of(Long userId, String userIp, String userAgent) {
        return new RequestContext(userId, userIp, userAgent);
    }
}