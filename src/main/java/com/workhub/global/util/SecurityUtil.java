package com.workhub.global.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Optional;

/**
 * Spring Security 관련 유틸리티 클래스
 * 세션 기반 인증에서 사용자 정보 조회, 권한 확인 등의 기능 제공
 */
@Slf4j
public final class SecurityUtil {

    private SecurityUtil() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * 현재 인증된 Authentication 객체를 반환
     *
     * @return Optional<Authentication>
     */
    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * 현재 인증된 사용자의 Principal 객체를 반환
     *
     * @return Optional<Object>
     */
    public static Optional<Object> getPrincipal() {
        return getAuthentication()
                .map(Authentication::getPrincipal);
    }

    /**
     * 현재 인증된 사용자의 UserDetails 객체를 반환
     *
     * @return Optional<UserDetails>
     */
    public static Optional<UserDetails> getCurrentUserDetails() {
        return getPrincipal()
                .filter(principal -> principal instanceof UserDetails)
                .map(principal -> (UserDetails) principal);
    }

    /**
     * 현재 인증된 사용자의 username(로그인 ID)을 반환
     *
     * @return Optional<String>
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUserDetails()
                .map(UserDetails::getUsername);
    }

    /**
     * 현재 인증된 사용자의 username을 반환 (없으면 예외 발생)
     *
     * @return String
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static String getCurrentUsernameOrThrow() {
        return getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자가 없습니다."));
    }

    /**
     * 현재 인증된 사용자의 ID를 반환
     * TODO: User 엔티티 생성 후 구현
     *
     * @return Optional<Long>
     */
    public static Optional<Long> getCurrentUserId() {
        // TODO: UserDetails 구현체에서 사용자 ID 추출
        // 예시:
        // return getCurrentUserDetails()
        //         .filter(userDetails -> userDetails instanceof CustomUserDetails)
        //         .map(userDetails -> ((CustomUserDetails) userDetails).getUserId());
        log.warn("getCurrentUserId() is not implemented yet. User entity is required.");
        return Optional.empty();
    }

    /**
     * 현재 인증된 사용자의 ID를 반환 (없으면 예외 발생)
     * TODO: User 엔티티 생성 후 구현
     *
     * @return Long
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static Long getCurrentUserIdOrThrow() {
        return getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자가 없습니다."));
    }

    /**
     * 현재 인증된 User 엔티티 객체를 반환
     * TODO: User 엔티티 생성 후 구현
     *
     * @return Optional<User>
     */
    // public static Optional<User> getCurrentUser() {
    //     // TODO: UserDetails 구현체에서 User 엔티티 추출 또는 DB 조회
    //     // 예시:
    //     // return getCurrentUserId()
    //     //         .flatMap(userId -> userRepository.findById(userId));
    //     log.warn("getCurrentUser() is not implemented yet. User entity is required.");
    //     return Optional.empty();
    // }

    /**
     * 사용자가 인증되었는지 확인
     *
     * @return boolean
     */
    public static boolean isAuthenticated() {
        return getAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }

    /**
     * 익명 사용자인지 확인
     *
     * @return boolean
     */
    public static boolean isAnonymous() {
        return !isAuthenticated();
    }

    /**
     * 현재 사용자가 특정 역할(Role)을 가지고 있는지 확인
     *
     * @param role 확인할 역할 (ROLE_ 접두사 포함/미포함 모두 가능)
     * @return boolean
     */
    public static boolean hasRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(authority -> authority.equals(roleWithPrefix)))
                .orElse(false);
    }

    /**
     * 현재 사용자가 여러 역할 중 하나라도 가지고 있는지 확인
     *
     * @param roles 확인할 역할들
     * @return boolean
     */
    public static boolean hasAnyRole(String... roles) {
        return Arrays.stream(roles)
                .anyMatch(SecurityUtil::hasRole);
    }

    /**
     * 현재 사용자가 모든 역할을 가지고 있는지 확인
     *
     * @param roles 확인할 역할들
     * @return boolean
     */
    public static boolean hasAllRoles(String... roles) {
        return Arrays.stream(roles)
                .allMatch(SecurityUtil::hasRole);
    }

    /**
     * 현재 사용자가 특정 권한(Authority)을 가지고 있는지 확인
     *
     * @param authority 확인할 권한
     * @return boolean
     */
    public static boolean hasAuthority(String authority) {
        return getAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> a.equals(authority)))
                .orElse(false);
    }

    /**
     * 현재 사용자가 여러 권한 중 하나라도 가지고 있는지 확인
     *
     * @param authorities 확인할 권한들
     * @return boolean
     */
    public static boolean hasAnyAuthority(String... authorities) {
        return Arrays.stream(authorities)
                .anyMatch(SecurityUtil::hasAuthority);
    }

    /**
     * 현재 사용자가 모든 권한을 가지고 있는지 확인
     *
     * @param authorities 확인할 권한들
     * @return boolean
     */
    public static boolean hasAllAuthorities(String... authorities) {
        return Arrays.stream(authorities)
                .allMatch(SecurityUtil::hasAuthority);
    }

    /**
     * 현재 세션 ID를 반환
     *
     * @return Optional<String>
     */
    public static Optional<String> getSessionId() {
        return getCurrentRequest()
                .map(request -> request.getSession(false))
                .map(session -> session.getId());
    }

    /**
     * 현재 요청의 HttpServletRequest를 반환
     *
     * @return Optional<HttpServletRequest>
     */
    public static Optional<HttpServletRequest> getCurrentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(attributes -> attributes instanceof ServletRequestAttributes)
                .map(attributes -> ((ServletRequestAttributes) attributes).getRequest());
    }

    /**
     * 현재 요청의 IP 주소를 반환
     *
     * @return Optional<String>
     */
    public static Optional<String> getRemoteAddr() {
        return getCurrentRequest()
                .map(request -> {
                    String ip = request.getHeader("X-Forwarded-For");
                    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                        ip = request.getHeader("Proxy-Client-IP");
                    }
                    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                        ip = request.getHeader("WL-Proxy-Client-IP");
                    }
                    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                        ip = request.getHeader("HTTP_CLIENT_IP");
                    }
                    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
                    }
                    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                        ip = request.getRemoteAddr();
                    }
                    return ip;
                });
    }

    /**
     * 현재 요청의 User-Agent를 반환
     *
     * @return Optional<String>
     */
    public static Optional<String> getUserAgent() {
        return getCurrentRequest()
                .map(request -> request.getHeader("User-Agent"));
    }

    /**
     * 현재 요청의 Referer를 반환
     *
     * @return Optional<String>
     */
    public static Optional<String> getReferer() {
        return getCurrentRequest()
                .map(request -> request.getHeader("Referer"));
    }
}