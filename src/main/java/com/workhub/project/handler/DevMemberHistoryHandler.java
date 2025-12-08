package com.workhub.project.handler;

import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryHandler;
import com.workhub.project.entity.ProjectDevMemberHistory;
import com.workhub.project.repository.DevMemberHistoryRepository;
import org.springframework.stereotype.Component;

@Component
public class DevMemberHistoryHandler extends HistoryHandler {

    public DevMemberHistoryHandler(DevMemberHistoryRepository repository) {
        super(repository, ProjectDevMemberHistory::of);
    }

    @Override
    public HistoryType getType() {
        return HistoryType.PROJECT_DEV_MEMBER;
    }
}
