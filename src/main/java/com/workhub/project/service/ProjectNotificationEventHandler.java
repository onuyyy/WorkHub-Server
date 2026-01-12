package com.workhub.project.service;

import com.workhub.project.event.ProjectCreatedEvent;
import com.workhub.project.event.ProjectStatusChangedEvent;
import com.workhub.project.event.ProjectUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectNotificationEventHandler {

    private final ProjectEventNotificationService projectEventNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProjectCreated(ProjectCreatedEvent event) {
        projectEventNotificationService.notifyProjectCreated(event.project());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStatusChanged(ProjectStatusChangedEvent event) {
        projectEventNotificationService.notifyStatusChanged(event.project(), event.previousStatus());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProjectUpdated(ProjectUpdatedEvent event) {
        var before = event.before();
        var after = event.after();

        boolean titleChanged = !before.title().equals(after.getProjectTitle());
        boolean descChanged = !before.description().equals(after.getProjectDescription());
        boolean endDateChanged = (before.endDate() == null && after.getContractEndDate() != null)
                || (before.endDate() != null && !before.endDate().equals(after.getContractEndDate()));

        if (titleChanged || descChanged || endDateChanged) {
            projectEventNotificationService.notifyInfoUpdated(after, titleChanged, descChanged, endDateChanged);
        }

        int addedCount = countAdded(before.clientIds(), event.requestedClientIds())
                + countAdded(before.devIds(), event.requestedDevIds());
        if (addedCount > 0) {
            projectEventNotificationService.notifyMembersAdded(after, addedCount);
        }

        int removedCount = countRemoved(before.clientIds(), event.requestedClientIds())
                + countRemoved(before.devIds(), event.requestedDevIds());
        if (removedCount > 0) {
            projectEventNotificationService.notifyMembersRemoved(after, removedCount);
        }
    }

    private int countAdded(List<Long> before, List<Long> after) {
        return (int) after.stream()
                .filter(id -> !before.contains(id))
                .count();
    }

    private int countRemoved(List<Long> before, List<Long> after) {
        return (int) before.stream()
                .filter(id -> !after.contains(id))
                .count();
    }
}
