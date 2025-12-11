package com.workhub.projectNode.dto;

import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;

public record CreateNodeRequest(
        @NotEmpty
        String title,
        @NotEmpty
        String description,
        @NotEmpty
        Long developerUserId,
        @NotEmpty
        LocalDate startDate,
        @NotEmpty
        LocalDate endDate
) {
}
