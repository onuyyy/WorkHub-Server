package com.workhub.userTable.entity;

import com.workhub.global.entity.BaseTimeEntity;
import com.workhub.userTable.dto.UserRegisterRecord;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_table")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTable extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", nullable = false, length = 30)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "phone", nullable = false, length = 12)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "lasted_at")
    private LocalDateTime lastedAt;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.lastedAt = LocalDateTime.now();
    }

    public void updateRole(UserRole newRole) {
        this.role = newRole;
    }

    public void updateStatus(Status newStatus) {
        this.status = newStatus;
        this.lastedAt = LocalDateTime.now();
    }

    public static UserTable of(UserRegisterRecord register, String encodedPassword) {
        return UserTable.builder()
                .loginId(register.loginId())
                .password(encodedPassword)
                .email(register.email())
                .phone(register.phone())
                .userName(register.userName())
                .role(register.role())
                .companyId(register.companyId())
                .status(Status.ACTIVE)
                .build();
    }
}
