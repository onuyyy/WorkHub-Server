package com.workhub.project.event;

import com.workhub.project.entity.Project;

import java.time.LocalDate;
import java.util.List;

public record ProjectUpdatedEvent(
        ProjectUpdateSnapshot before,
        Project after,
        List<Long> requestedClientIds,
        List<Long> requestedDevIds
) {

    public record ProjectUpdateSnapshot(
            String title,
            String description,
            LocalDate endDate,
            List<Long> clientIds,
            List<Long> devIds
    ) {}
}
