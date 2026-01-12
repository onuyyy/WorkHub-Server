package com.workhub.userTable.dto.company.response;

import com.workhub.userTable.entity.Company;
import lombok.Builder;

@Builder
public record CompanyTitleResponse(
        Long companyId,
        String companyName
) {
    public static CompanyTitleResponse from(Company company) {
        return CompanyTitleResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .build();
    }
}
