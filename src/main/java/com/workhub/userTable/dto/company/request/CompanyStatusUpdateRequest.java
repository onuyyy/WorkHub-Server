package com.workhub.userTable.dto.company.request;

import com.workhub.userTable.entity.CompanyStatus;
import jakarta.validation.constraints.NotNull;

public record CompanyStatusUpdateRequest(
        @NotNull
        CompanyStatus status
) {
}
