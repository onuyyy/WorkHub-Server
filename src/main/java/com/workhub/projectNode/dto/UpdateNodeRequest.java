package com.workhub.projectNode.dto;

import com.workhub.projectNode.entity.Priority;

import java.time.LocalDate;

public record UpdateNodeRequest(
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Priority priority
) {
}
