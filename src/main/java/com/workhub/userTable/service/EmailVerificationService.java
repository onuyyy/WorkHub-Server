package com.workhub.userTable.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.util.SecurityUtil;
import com.workhub.userTable.entity.EmailVerification;
import com.workhub.userTable.repository.EmailVerificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationRepository verificationRepository;
    private final JavaMailSender mailSender;

    @Value("${workhub.mail.verification-expiry-minutes:10}")
    private int expiryMinutes;

    @Value("${spring.mail.username:no-reply@workhub.com}")
    private String fromAddress;

    @Transactional
    public void sendVerificationCode(String email, String userName) {
        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
        String normalizedEmail = email.toLowerCase();
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);

        EmailVerification verification = verificationRepository.findByEmail(normalizedEmail)
                .map(existing -> {
                    existing.refresh(code, expiresAt);
                    return existing;
                })
                .orElseGet(() -> EmailVerification.of(normalizedEmail, code, expiresAt));

        verificationRepository.save(verification);
        sendVerificationMail(normalizedEmail, userName, code);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        LocalDateTime now = LocalDateTime.now();
        return verificationRepository.findByEmail(email.toLowerCase())
                .filter(verification -> !verification.isExpired(now))
                .filter(verification -> verification.getCode().equals(code))
                .map(verification -> {
                    verification.markVerified();
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isVerified(String email) {
        return verificationRepository.findByEmail(email.toLowerCase())
                .map(EmailVerification::isVerified)
                .orElse(false);
    }

    @Transactional
    public void consumeVerification(String email) {
        verificationRepository.deleteByEmail(email.toLowerCase());
    }

    private void sendVerificationMail(String to, String userName, String code) {
        String sender = StringUtils.hasText(fromAddress) ? fromAddress : to;
        String displayName = displayName(userName);
        String subject = "WorkHub 이메일 인증번호 안내";
        String content = buildPlainTextBody(displayName, code);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setFrom(sender);
            helper.setSubject(subject);
            helper.setText(content, false);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send verification mail to {}", to, e);
            throw new BusinessException("이메일을 전송하지 못했습니다.", e, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String displayName(String userName) {
        return StringUtils.hasText(userName) ? userName : "회원님";
    }

    private String buildPlainTextBody(String userName, String code) {
        return "안녕하세요, WorkHub입니다.\n\n" +
                userName + " 님의 이메일 인증번호는 " + code + " 입니다.\n" +
                expiryMinutes + "분 이내에 입력해 주세요.";
    }
}
