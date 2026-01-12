package com.workhub.project.handler;

import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryHandler;
import com.workhub.project.entity.ProjectHistory;
import com.workhub.project.repository.ProjectHistoryRepository;
import org.springframework.stereotype.Component;

@Component
public class ProjectHistoryHandler extends HistoryHandler {

    public ProjectHistoryHandler(ProjectHistoryRepository repository) {
        super(repository, ProjectHistory::of);
    }

    @Override
    public HistoryType getType() {
        return HistoryType.PROJECT;
    }
}