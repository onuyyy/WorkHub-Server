package com.workhub.userTable.entity;

import com.workhub.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmailVerification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_verification_id")
    private Long id;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 6, name = "verification_code")
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, name = "is_verified")
    private boolean verified;

    public void refresh(String newCode, LocalDateTime newExpiresAt) {
        this.code = newCode;
        this.expiresAt = newExpiresAt;
        this.verified = false;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public void markVerified() {
        this.verified = true;
    }

    public static EmailVerification of(String email, String code, LocalDateTime expiresAt) {
        return EmailVerification.builder()
                .email(email)
                .code(code)
                .expiresAt(expiresAt)
                .verified(false)
                .build();
    }
}
