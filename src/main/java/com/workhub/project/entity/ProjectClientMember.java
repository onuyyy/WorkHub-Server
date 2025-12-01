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
@Table(name = "project_client_member")
public class ProjectClientMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_client_member_id")
    private Long projectClientMemberId;

    @Column(name = "assigned_at")
    private LocalDate assignedAt;

    @Column(name = "removed_at")
    private LocalDate removedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "project_id")
    private Long projectId;

    public static ProjectClientMember of(Long userId, Long projectId) {
        return ProjectClientMember.builder()
                .assignedAt(LocalDate.now())
                .role(Role.READ) // todo : 추후 FE에서 어떻게 넘겨줄 것인지 정해야 합니다.
                .userId(userId)
                .projectId(projectId)
                .build();
    }

}
