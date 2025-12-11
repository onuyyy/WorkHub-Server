package com.workhub.userTable.repository;

import com.workhub.userTable.entity.Company;
import com.workhub.userTable.entity.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByCompanyNumber(String companyNumber);

    List<Company> findAllByCompanystatus(CompanyStatus status);

    Optional<Company> findByCompanyIdAndCompanystatus(Long companyId, CompanyStatus status);
}
