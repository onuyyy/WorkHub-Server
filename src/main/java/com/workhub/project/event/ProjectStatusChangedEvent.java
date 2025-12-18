package com.workhub.project.event;

import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;

public record ProjectStatusChangedEvent(Project project, Status previousStatus) {
}
