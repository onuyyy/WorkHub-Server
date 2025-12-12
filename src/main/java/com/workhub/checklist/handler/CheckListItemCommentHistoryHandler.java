package com.workhub.checklist.handler;

import com.workhub.checklist.entity.comment.CheckListItemCommentHistory;
import com.workhub.checklist.repository.CheckListItemCommentHistoryRepository;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryHandler;
import org.springframework.stereotype.Component;

@Component
public class CheckListItemCommentHistoryHandler extends HistoryHandler {

    public CheckListItemCommentHistoryHandler(CheckListItemCommentHistoryRepository repository) {
        super(repository, CheckListItemCommentHistory::of);
    }

    @Override
    public HistoryType getType() {
        return HistoryType.CHECK_LIST_ITEM_COMMENT;
    }
}
