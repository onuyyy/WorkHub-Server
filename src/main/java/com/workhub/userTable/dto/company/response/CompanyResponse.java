package com.workhub.userTable.dto.company.response;

import com.workhub.userTable.entity.Company;
import com.workhub.userTable.entity.CompanyStatus;

public record CompanyResponse(
        Long companyId,
        String companyName,
        String companyNumber,
        String tel,
        String address,
        CompanyStatus status
) {
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getCompanyId(),
                company.getCompanyName(),
                company.getCompanyNumber(),
                company.getTel(),
                company.getAddress(),
                company.getCompanystatus()
        );
    }
}
