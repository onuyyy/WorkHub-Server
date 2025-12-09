package com.workhub.userTable.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.userTable.dto.CompanyRegisterRequest;
import com.workhub.userTable.dto.CompanyResponse;
import com.workhub.userTable.entity.Company;
import com.workhub.userTable.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyResponse registerCompany(CompanyRegisterRequest request) {
        validateDuplicateCompanyNumber(request.companyNumber());

        Company company = Company.of(request);
        Company savedCompany = companyRepository.save(company);

        return CompanyResponse.from(savedCompany);
    }

    private void validateDuplicateCompanyNumber(String companyNumber) {
        if (companyRepository.existsByCompanyNumber(companyNumber)) {
            throw new BusinessException(ErrorCode.COMPANY_ALREADY_EXISTS);
        }
    }
}
