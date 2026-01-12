package com.workhub.projectNode.dto;

import com.workhub.projectNode.entity.NodeCategory;

import java.time.LocalDate;

public record UpdateNodeRequest(
        String title,
        NodeCategory nodeCategory,
        String description,
        Long developerUserId,
        LocalDate startDate,
        LocalDate endDate
) {
}
