package com.workhub.checklist.handler;

import com.workhub.checklist.entity.checkList.CheckListItemHistory;
import com.workhub.checklist.repository.CheckListItemHistoryRepository;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryHandler;
import org.springframework.stereotype.Component;

@Component
public class CheckListItemHistoryHandler extends HistoryHandler {

    public CheckListItemHistoryHandler(CheckListItemHistoryRepository repository) {
        super(repository, CheckListItemHistory::of);
    }

    @Override
    public HistoryType getType() {
        return HistoryType.CHECK_LIST_ITEM;
    }
}
