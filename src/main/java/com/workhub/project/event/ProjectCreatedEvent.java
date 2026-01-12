package com.workhub.project.event;

import com.workhub.project.entity.Project;

public record ProjectCreatedEvent(Project project) {
}
