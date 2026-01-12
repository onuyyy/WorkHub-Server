package com.workhub.userTable.config;

import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.repository.UserRepository;
import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserTable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${workhub.admin.bootstrap-enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${workhub.admin.login-id:admin}")
    private String loginId;

    @Value("${workhub.admin.password:Admin!234}")
    private String password;

    @Value("${workhub.admin.email:admin@workhub.com}")
    private String email;

    @Value("${workhub.admin.phone:01000000000}")
    private String phone;

    @Value("${workhub.admin.company-id:1}")
    private Long companyId;

    @Value("${workhub.admin.user-name:WorkHub Admin}")
    private String userName;

    @PostConstruct
    public void createDefaultAdminIfNecessary() {
        if (!bootstrapEnabled) {
            log.debug("Admin bootstrap disabled. Skipping default admin creation.");
            return;
        }

        if (userRepository.existsByLoginId(loginId)) {
            log.debug("Admin user already exists. Skipping bootstrap.");
            return;
        }

        UserTable adminUser = UserTable.builder()
                .loginId(loginId)
                .password(passwordEncoder.encode(password))
                .email(email)
                .phone(phone)
                .userName(userName)
                .companyId(companyId)
                .role(UserRole.ADMIN)
                .status(Status.ACTIVE)
                .companyId(companyId)
                .build();

        userRepository.save(adminUser);
        log.info("Default admin user created with loginId: {}", loginId);
    }
}
