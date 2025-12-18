package com.workhub.projectNode.event;

public record ProjectNodeApprovedEvent(
        Long projectId,
        Long projectNodeId,
        String title,
        String message
) {
}
