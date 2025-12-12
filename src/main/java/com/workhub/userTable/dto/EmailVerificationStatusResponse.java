package com.workhub.userTable.dto;

public record EmailVerificationStatusResponse(
        boolean verified
) {
    public static EmailVerificationStatusResponse of(boolean verified) {
        return new EmailVerificationStatusResponse(verified);
    }
}
