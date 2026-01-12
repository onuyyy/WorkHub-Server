package com.workhub.projectNode.dto;

import com.workhub.projectNode.entity.NodeCategory;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;

public record CreateNodeRequest(
        @NotEmpty
        String title,
        @NotEmpty
        NodeCategory nodeCategory,
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
