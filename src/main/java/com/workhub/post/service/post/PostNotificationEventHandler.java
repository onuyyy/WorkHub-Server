package com.workhub.post.service.post;

import com.workhub.post.event.PostCreatedEvent;
import com.workhub.post.event.PostUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class PostNotificationEventHandler {

    private final PostNotificationService postNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostCreated(PostCreatedEvent event) {
        postNotificationService.notifyCreated(event.projectId(), event.post());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostUpdated(PostUpdatedEvent event) {
        postNotificationService.notifyUpdated(event.projectId(), event.post());
    }
}
