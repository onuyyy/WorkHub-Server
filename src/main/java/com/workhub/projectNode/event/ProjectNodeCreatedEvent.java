package com.workhub.projectNode.event;

import com.workhub.projectNode.entity.ProjectNode;

public record ProjectNodeCreatedEvent(Long projectId, ProjectNode node) {
}
