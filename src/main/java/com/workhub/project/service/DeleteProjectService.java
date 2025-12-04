package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.project.entity.Project;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeleteProjectService {

    private final ProjectService projectService;
    private final HistoryRecorder historyRecorder;

    public void deleteProject(Long projectId) {

        Project project = projectService.findProjectById(projectId);
        String beforeStatus = project.getStatus().toString();

        project.markDeleted();
        historyRecorder.recordHistory(HistoryType.PROJECT, projectId, ActionType.DELETE,
                beforeStatus);

    }

}
