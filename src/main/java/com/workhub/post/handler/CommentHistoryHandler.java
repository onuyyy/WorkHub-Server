package com.workhub.post.handler;

import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryHandler;
import com.workhub.post.entity.CommentHistory;
import com.workhub.post.repository.comment.CommentHistoryRepository;
import org.springframework.stereotype.Component;

@Component
public class CommentHistoryHandler extends HistoryHandler {

    public CommentHistoryHandler(CommentHistoryRepository repository) {
        super(repository, CommentHistory::of);
    }

    @Override
    public HistoryType getType() {
        return HistoryType.POST_COMMENT;
    }
}

