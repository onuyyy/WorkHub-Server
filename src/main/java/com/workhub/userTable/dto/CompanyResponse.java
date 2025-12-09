package com.workhub.userTable.dto;

import com.workhub.userTable.entity.Company;

public record CompanyResponse(
        Long companyId,
        String companyName,
        String companyNumber,
        String tel,
        String address
) {
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getCompanyId(),
                company.getCompanyName(),
                company.getCompanyNumber(),
                company.getTel(),
                company.getAddress()
        );
    }
}
