package com.workhub.projectNode.dto;

public record CreateNodeRequest(
        String title,
        String description,
        Integer nodeOrder,
        String priority
) {
}
