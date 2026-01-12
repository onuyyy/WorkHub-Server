package com.workhub.post.service.comment;

import com.workhub.post.event.CommentCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class CommentNotificationEventHandler {

    private final CommentNotificationService commentNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentCreated(CommentCreatedEvent event) {
        commentNotificationService.notifyCreated(event.projectId(), event.post(), event.comment());
    }
}
