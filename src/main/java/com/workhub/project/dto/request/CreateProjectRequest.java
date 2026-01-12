package com.workhub.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record CreateProjectRequest(

        @NotBlank(message = "프로젝트명은 빈값일 수 없습니다.")
        String projectName,
        @NotBlank(message = "프로젝트 설명은 빈값일 수 없습니다.")
        String projectDescription,
        @NotEmpty
        Long company,
        @NotEmpty
        List<Long> managerIds,
        @NotEmpty
        List<Long> developerIds,
        @NotEmpty
        LocalDate starDate,
        @NotEmpty
        LocalDate endDate
) {
}
