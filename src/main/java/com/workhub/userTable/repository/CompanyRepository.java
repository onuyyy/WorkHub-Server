package com.workhub.userTable.repository;

import com.workhub.userTable.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByCompanyNumber(String companyNumber);
}
