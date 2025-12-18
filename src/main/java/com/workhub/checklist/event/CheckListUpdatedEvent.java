package com.workhub.checklist.event;

public record CheckListUpdatedEvent(Long projectId, Long nodeId, String message) {
}
