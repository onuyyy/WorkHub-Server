package com.workhub.projectNode.event;

public record ProjectNodeUpdatedEvent(
        Long projectId,
        Long projectNodeId,
        String title,
        String changedDesc
) {
}
