package com.workhub.userTable.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import com.workhub.project.repository.ProjectRepository;
import com.workhub.userTable.dto.company.request.CompanyRegisterRequest;
import com.workhub.userTable.dto.company.response.CompanyDetailResponse;
import com.workhub.userTable.dto.company.response.CompanyListResponse;
import com.workhub.userTable.dto.company.response.CompanyResponse;
import com.workhub.userTable.dto.company.response.CompanyTitleResponse;
import com.workhub.userTable.entity.Company;
import com.workhub.userTable.entity.CompanyStatus;
import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.repository.CompanyRepository;
import com.workhub.userTable.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

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
    public Page<CompanyListResponse> getCompanies(Pageable pageable) {
        Page<Company> companies = companyRepository.findAllByCompanystatus(CompanyStatus.ACTIVE, pageable);

        return companies.map(company -> {
            // 해당 회사의 프로젝트 조회
            List<Project> projects = projectRepository.findAllByClientCompanyId(company.getCompanyId());

            // IN_PROGRESS와 COMPLETED 카운트
            long inProgressCount = projects.stream()
                    .filter(p -> p.getStatus() == Status.IN_PROGRESS)
                    .count();
            long completedCount = projects.stream()
                    .filter(p -> p.getStatus() == Status.COMPLETED)
                    .count();

            // 활성 클라이언트 멤버 수 조회 (company_id 기준)
            long clientMemberCount = userRepository.countByCompanyIdAndRoleAndStatus(
                    company.getCompanyId(),
                    UserRole.CLIENT,
                    com.workhub.userTable.entity.Status.ACTIVE
            );

            // CompanyListResponse 생성
            return CompanyListResponse.from(company, inProgressCount, completedCount, clientMemberCount);
        });
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

    @Transactional(readOnly = true)
    public Long countActiveCompanies() {
        return companyRepository.countByCompanystatus(CompanyStatus.ACTIVE);
    }
}
