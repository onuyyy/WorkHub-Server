package com.workhub.userTable.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.userTable.dto.company.request.CompanyRegisterRequest;
import com.workhub.userTable.dto.company.response.CompanyDetailResponse;
import com.workhub.userTable.dto.company.response.CompanyListResponse;
import com.workhub.userTable.dto.company.response.CompanyResponse;
import com.workhub.userTable.dto.company.response.CompanyTitleResponse;
import com.workhub.userTable.entity.Company;
import com.workhub.userTable.entity.CompanyStatus;
import com.workhub.userTable.repository.CompanyRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Transactional(readOnly = true)
    public List<CompanyListResponse> getCompanys() {
        return companyRepository.findAllByCompanystatus(CompanyStatus.ACTIVE).stream()
                .map(CompanyListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompanyTitleResponse> getCompanyNameList() {
        return companyRepository.findAllByCompanystatus(CompanyStatus.ACTIVE).stream()
                .map(CompanyTitleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyDetailResponse getCompany(Long companyId) {
        Company company = companyRepository.findByCompanyIdAndCompanystatus(companyId, CompanyStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.Company_NOT_EXISTS));
        return CompanyDetailResponse.from(company);
    }

    @Transactional(readOnly = true)
    public Company findById(Long companyId) {
        return companyRepository.findByCompanyIdAndCompanystatus(companyId, CompanyStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.Company_NOT_EXISTS));
    }

    @Transactional
    public void deleteCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.Company_NOT_EXISTS));
        company.markDeleted();
    }

    @Transactional
    public CompanyResponse updateCompanyStatus(Long companyId, CompanyStatus status) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.Company_NOT_EXISTS));
        company.updateStatus(status);
        return CompanyResponse.from(company);
    }

    /**
     * 여러 companyId로 Company 맵을 배치 조회
     *
     * @param companyIds 조회할 Company ID 리스트
     * @return companyId를 키로 하는 Company 맵
     */
    @Transactional(readOnly = true)
    public Map<Long, Company> getCompanyMapByCompanyIdIn(List<Long> companyIds) {
        if (companyIds == null || companyIds.isEmpty()) {
            return Map.of();
        }

        return companyRepository.findAllByCompanyIdInAndCompanystatus(companyIds, CompanyStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(Company::getCompanyId, company -> company));
    }
}
