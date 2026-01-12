package com.workhub.cs.handler;

import com.workhub.cs.entity.CsQnaHistory;
import com.workhub.cs.repository.csQna.CsQnaHistoryRepository;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryHandler;
import org.springframework.stereotype.Component;

@Component
public class CsQnaHistoryHandler extends HistoryHandler {

    public CsQnaHistoryHandler(CsQnaHistoryRepository repository) {
        super(repository, CsQnaHistory::of);
    }

    @Override
    public HistoryType getType() {
        return HistoryType.CS_QNA;
    }
}
