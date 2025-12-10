package com.workhub.projectNode.dto;

import java.time.LocalDate;

public record UpdateNodeRequest(
        String title,
        String description,
        Long developerUserId,
        LocalDate startDate,
        LocalDate endDate
) {
}
