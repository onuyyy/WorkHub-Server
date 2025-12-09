package com.workhub.project.dto.response;

import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import com.workhub.userTable.entity.Company;
import com.workhub.userTable.entity.UserTable;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record ProjectListResponse(
        Long projectId,
        String projectTitle,
        String projectDescription,
        Status status,
        LocalDate contractStartDate,
        LocalDate contractEndDate,
        CompanyResponse company,
        List<DevMembers> devMembers,
        List<ClientMembers> clientMembers,
        Integer totalMembers,
        Long workflowStep
) {
    @Builder
    public static record CompanyResponse (
        Long companyId,
        String companyName
    ){
        public static CompanyResponse from(Company company) {
            return CompanyResponse.builder()
                    .companyId(company.getCompanyId())
                    .companyName(company.getCompanyName())
                    .build();
        }
    }

    @Builder
    public static record DevMembers (
            Long devMemberId,
            String devMemberName
    ){
        public static DevMembers from(UserTable user) {
            return DevMembers.builder()
                    .devMemberId(user.getUserId())
                    .devMemberName("김개발자")  // todo : 추후 UserTable에서 사용자 이름 컬럼 추가 후 변경.
                    .build();
        }
    }

    @Builder
    public static record ClientMembers (
            Long clientMemberId,
            String clientMemberName
    ){
        public static ClientMembers from(UserTable user) {
            return ClientMembers.builder()
                    .clientMemberId(user.getUserId())
                    .clientMemberName("김매니저")  // todo : 추후 UserTable에서 사용자 이름 컬럼 추가 후 변경.
                    .build();
        }
    }

    public static ProjectListResponse from(Project project, List<UserTable> clientList, List<UserTable> devList,
                                           Long workflow, Company company) {

        CompanyResponse respCompany = CompanyResponse.from(company);
        List<DevMembers> devMembers = devList.stream()
                .map(DevMembers::from)
                .toList();

        List<ClientMembers> clientMembers = clientList.stream()
                .map(ClientMembers::from)
                .toList();

        Integer totalMembers = clientList.size() + devList.size();

        return ProjectListResponse.builder()
                .projectId(project.getProjectId())
                .projectTitle(project.getProjectTitle())
                .projectDescription(project.getProjectDescription())
                .status(project.getStatus())
                .contractStartDate(project.getContractStartDate())
                .contractEndDate(project.getContractEndDate())
                .company(respCompany)
                .devMembers(devMembers)
                .clientMembers(clientMembers)
                .totalMembers(totalMembers)
                .workflowStep(workflow)
                .build();
    }
}
