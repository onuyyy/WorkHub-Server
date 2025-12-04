package com.workhub.projectNode.dto;

import com.workhub.projectNode.entity.Priority;

public record CreateNodeRequest(
        String title,
        String description,
        Priority priority
) {
}
