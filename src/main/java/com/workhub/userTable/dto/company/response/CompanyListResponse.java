package com.workhub.userTable.dto.company.response;

import com.workhub.userTable.entity.Company;
import com.workhub.userTable.entity.CompanyStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CompanyListResponse (
        Long companyId,
        String companyName,
        String companyNumber,
        String tel,
        String address,
        LocalDateTime createdAt,
        CompanyStatus status,
        ProjectOverview projectOverview
){
    @Builder
    public record ProjectOverview (
            Long inProgressProject,
            Long completeProject,
            Long clientMember
    ) {
        public static ProjectOverview of(Long inProgressProject, Long completeProject, Long clientMember) {
            return ProjectOverview.builder()
                    .inProgressProject(inProgressProject)
                    .completeProject(completeProject)
                    .clientMember(clientMember)
                    .build();
        }
    }

    public static CompanyListResponse from(Company company, Long inProgressCount, Long completedCount, Long clientMemberCount){
        ProjectOverview projectOverview = ProjectOverview.of(inProgressCount, completedCount, clientMemberCount);

        return CompanyListResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .companyNumber(company.getCompanyNumber())
                .tel(company.getTel())
                .address(company.getAddress())
                .createdAt(company.getCreatedAt())
                .status(company.getCompanystatus())
                .projectOverview(projectOverview)
                .build();
    }
}
