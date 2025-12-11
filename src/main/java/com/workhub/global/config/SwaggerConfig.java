package com.workhub.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Bean
    public OpenAPI openAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("WorkHub API")
                        .description("WorkHub API 문서")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("WorkHub Team")
                                .email("WorkHub@WorkHub.com")
                        )
                );

        // 프로필별 서버 URL 설정
        if ("local".equals(activeProfile)) {
            openAPI.servers(List.of(
                    new Server().url("http://localhost:8080").description("로컬 서버")
            ));
        } else {
            openAPI.servers(List.of(
                    new Server().url("https://workhub.o-r.kr").description("운영 서버"),
                    new Server().url("http://localhost:8080").description("로컬 서버")
            ));
        }

        return openAPI;
    }
}
