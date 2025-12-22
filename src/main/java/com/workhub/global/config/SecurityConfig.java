package com.workhub.global.config;

import com.workhub.global.security.CustomAccessDeniedHandler;
import com.workhub.global.security.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 활성화 (세션 기반 인증에 필수)
//                .csrf(csrf -> csrf
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                )
                .csrf(csrf -> csrf.disable())
                // CORS 설정
                .cors(Customizer.withDefaults())
                // 세션 관리 정책
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(10)
                        .maxSessionsPreventsLogin(false)
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                )
                // 예외 처리 설정
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)  // 인증되지 않은 경우 401
                        .accessDeniedHandler(accessDeniedHandler)  // 권한이 없는 경우 403
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/users/login").permitAll()
                        .requestMatchers("/api/v1/users/**").authenticated()

                        .requestMatchers("/api/v1/company/list").authenticated()
                        .requestMatchers("/api/v1/company/{companyId}/list").authenticated()
                        .requestMatchers("/api/v1/company/detail/{companyId}").authenticated()
                        .requestMatchers("/api/v1/company/**").hasRole("ADMIN")

                        .requestMatchers("/api/v1/projects/*/nodes/*/checkLists/**").authenticated()
                        .requestMatchers("/api/v1/projects/*/nodes/*/checkLists").hasAnyRole("DEVELOPER", "ADMIN")
                        .requestMatchers( "/api/v1/projects/*/nodes/*/checkLists").hasAnyRole("DEVELOPER", "ADMIN")
                        .requestMatchers( "/api/v1/projects/*/nodes/*/checkLists/*/items/*/status").hasRole("CLIENT")

                        .requestMatchers("/api/v1/projects/list").authenticated()
                        .requestMatchers("/api/v1/projects/*/status").hasAnyRole("DEVELOPER", "ADMIN")

                        .requestMatchers("/api/v1/projects/{projectId}/nodes/{nodeId}").authenticated()
                        .requestMatchers("/api/v1/projects/{projectId}/nodes/list").authenticated()
                        .requestMatchers("/api/v1/projects/{projectId}/nodes/**").hasAnyRole("DEVELOPER", "ADMIN")

                        .requestMatchers("/api/v1/notifications/**").authenticated()

                        .requestMatchers("/api/v1/projects/*/csPosts/**").hasAnyRole("CLIENT", "DEVELOPER", "ADMIN")

                        .requestMatchers("/api/v1/projects/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/users/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // frontend cors setting
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "https://work-hub-fe.vercel.app",
                "https://workhub.o-r.kr",
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        // SSE를 위한 추가 설정
        configuration.setExposedHeaders(Arrays.asList(
                "Last-Event-ID",  // SSE 재연결용
                "X-Content-Type-Options",
                "Cache-Control"
        ));
        configuration.setMaxAge(3600L);  // preflight 캐싱 1시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
