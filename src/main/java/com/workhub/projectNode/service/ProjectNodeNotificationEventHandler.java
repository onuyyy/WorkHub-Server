package com.workhub.projectNode.service;

import com.workhub.projectNode.event.ProjectNodeApprovedEvent;
import com.workhub.projectNode.event.ProjectNodeCreatedEvent;
import com.workhub.projectNode.event.ProjectNodeRejectedEvent;
import com.workhub.projectNode.event.ProjectNodeReviewRequestedEvent;
import com.workhub.projectNode.event.ProjectNodeUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class ProjectNodeNotificationEventHandler {

    private final ProjectNodeNotificationService projectNodeNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(ProjectNodeCreatedEvent event) {
        projectNodeNotificationService.notifyCreated(
                event.projectId(),
                event.node().getProjectNodeId(),
                event.node().getTitle()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUpdated(ProjectNodeUpdatedEvent event) {
        projectNodeNotificationService.notifyUpdated(
                event.projectId(),
                event.projectNodeId(),
                event.title(),
                event.changedDesc()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewRequested(ProjectNodeReviewRequestedEvent event) {
        projectNodeNotificationService.notifyPending(
                event.projectId(),
                event.projectNodeId(),
                event.title(),
                event.message()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApproved(ProjectNodeApprovedEvent event) {
        projectNodeNotificationService.notifyApproved(
                event.projectId(),
                event.projectNodeId(),
                event.title(),
                event.message()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRejected(ProjectNodeRejectedEvent event) {
        projectNodeNotificationService.notifyRejected(
                event.projectId(),
                event.projectNodeId(),
                event.title(),
                event.message()
        );
    }
}
