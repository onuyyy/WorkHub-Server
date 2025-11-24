package com.workhub.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 데이터베이스 초기화 순서를 제어하는 설정
 * 1. JPA가 먼저 테이블을 생성 (ddl-auto: update)
 * 2. Flyway가 인덱스를 생성
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseInitializationConfig implements ApplicationRunner, Ordered {

    private final Flyway flyway;  // Spring Boot가 생성한 Flyway bean 주입

    /**
     * 애플리케이션 시작 후 Flyway 마이그레이션 실행
     * JPA가 이미 테이블을 생성한 상태에서 인덱스를 추가
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting Flyway migration after JPA initialization...");
        flyway.migrate();
        log.info("Flyway migration completed successfully");
    }

    /**
     * 가장 나중에 실행되도록 우선순위 설정
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}