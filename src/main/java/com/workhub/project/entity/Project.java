package com.workhub.project.entity;


import com.workhub.global.entity.BaseTimeEntity;
import com.workhub.project.dto.CreateProjectRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project")
public class Project extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_title", length = 50, nullable = false)
    private String projectTitle;

    @Column(name = "project_description", columnDefinition = "TEXT")
    private String projectDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "client_company_id")
    private Long clientCompanyId;

    public static Project of (CreateProjectRequest request) {
        return Project.builder()
                .projectTitle(request.projectName())
                .projectDescription(request.projectDescription())
                .status(Status.CONTRACT)
                .contractStartDate(request.starDate())
                .contractEndDate(request.endDate())
                .clientCompanyId(request.company())
                .build();
    }
}
