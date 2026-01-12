package com.workhub.cs.handler;

import com.workhub.cs.entity.CsPostHistory;
import com.workhub.cs.repository.csPost.CsPostHistoryRepository;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryHandler;
import org.springframework.stereotype.Component;

@Component
public class CsPostHistoryHandler extends HistoryHandler {

    public CsPostHistoryHandler(CsPostHistoryRepository repository) {
        super(repository, CsPostHistory::of);
    }

    @Override
    public HistoryType getType() {
        return HistoryType.CS_POST;
    }
}