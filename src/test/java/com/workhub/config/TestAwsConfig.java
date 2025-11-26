package com.workhub.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestAwsConfig {

    @Bean
    public S3Client s3Client() {
        return mock(S3Client.class);
    }

    @Bean
    public Flyway flyway() {
        return mock(Flyway.class);
    }
}
