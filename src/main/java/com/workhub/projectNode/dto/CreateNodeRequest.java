package com.workhub.projectNode.dto;

import com.workhub.projectNode.entity.Priority;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;

public record CreateNodeRequest(
        @NotEmpty
        String title,
        @NotEmpty
        String description,
        @NotEmpty
        LocalDate starDate,
        @NotEmpty
        LocalDate endDate,
        @NotEmpty
        Priority priority
) {
}
