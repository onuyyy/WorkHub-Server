package com.workhub.projectNode.event;

public record ProjectNodeRejectedEvent(
        Long projectId,
        Long projectNodeId,
        String title,
        String message
) {
}
