package com.workhub.projectNode.event;

public record ProjectNodeReviewRequestedEvent(
        Long projectId,
        Long projectNodeId,
        String title,
        String message
) {
}
