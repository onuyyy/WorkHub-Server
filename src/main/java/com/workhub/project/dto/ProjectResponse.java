package com.workhub.project.dto;

import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ProjectResponse(
        Long projectId,
        String projectTitle,
        String projectDescription,
        Status status,
        LocalDate contractStartDate,
        LocalDate contractEndDate,
        Long company
) {
    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
                .projectId(project.getProjectId())
                .projectTitle(project.getProjectTitle())
                .projectDescription(project.getProjectDescription())
                .status(project.getStatus())
                .contractStartDate(project.getContractStartDate())
                .contractEndDate(project.getContractEndDate())
                .company(project.getClientCompanyId())
                .build();
    }
}
