package com.workhub.project.handler;

import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryHandler;
import com.workhub.project.entity.ProjectClientMemberHistory;
import com.workhub.project.repository.ClientMemberHistoryRepository;
import org.springframework.stereotype.Component;

@Component
public class ClientMemberHistoryHandler extends HistoryHandler {

    public ClientMemberHistoryHandler(ClientMemberHistoryRepository repository) { super(repository, ProjectClientMemberHistory::of); }

    @Override
    public HistoryType getType() {
        return HistoryType.PROJECT_CLIENT_MEMBER;
    }
}
