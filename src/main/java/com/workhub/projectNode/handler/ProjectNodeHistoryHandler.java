package com.workhub.projectNode.handler;

import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryHandler;
import com.workhub.projectNode.entity.ProjectNodeHistory;
import com.workhub.projectNode.repository.ProjectNodeHistoryRepository;
import org.springframework.stereotype.Component;

@Component
public class ProjectNodeHistoryHandler extends HistoryHandler {

    public ProjectNodeHistoryHandler(ProjectNodeHistoryRepository repository) {
        super(repository, ProjectNodeHistory::of);
    }

    @Override
    public HistoryType getType() {
        return HistoryType.PROJECT_NODE;
    }
}