package com.workhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WorkhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkhubApplication.class, args);
	}

	/**
	 * Spring Boot의 기본 FlywayMigrationInitializer를 비활성화
	 * DatabaseInitializationConfig에서 수동으로 제어하기 위함
	 */
	@Bean
	public FlywayMigrationInitializer flywayInitializer(org.flywaydb.core.Flyway flyway) {
		return new FlywayMigrationInitializer(flyway, (f) -> {
			// 아무것도 하지 않음 (마이그레이션을 실행하지 않음)
		});
	}

}
