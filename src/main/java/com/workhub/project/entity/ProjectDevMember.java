package com.workhub.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_dev_member")
public class ProjectDevMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_dev_member_id")
    private Long projectMemberId;

    @Column(name = "assigned_at")
    private LocalDate assignedAt;

    @Column(name = "removed_at")
    private LocalDate removedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "dev_part")
    private DevPart devPart;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "project_id")
    private Long projectId;

    public static ProjectDevMember of(Long userId, Long projectId) {
        return ProjectDevMember.builder()
                .assignedAt(LocalDate.now())
                .devPart(DevPart.BE)   // todo : 추후 FE에서 어떻게 값을 전달할지 결정해야 합니다.
                .userId(userId)
                .projectId(projectId)
                .build();
    }

}
