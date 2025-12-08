package com.workhub.post.handler;

import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryHandler;
import com.workhub.post.entity.PostHistory;
import com.workhub.post.repository.post.PostHistoryRepository;
import org.springframework.stereotype.Component;

@Component
public class PostHistoryHandler extends HistoryHandler {

    public PostHistoryHandler(PostHistoryRepository repository) {
        super(repository, PostHistory::of);
    }

    @Override
    public HistoryType getType() {
        return HistoryType.POST;
    }
}
