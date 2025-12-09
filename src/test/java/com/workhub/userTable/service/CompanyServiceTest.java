package com.workhub.userTable.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.userTable.dto.CompanyRegisterRequest;
import com.workhub.userTable.dto.CompanyResponse;
import com.workhub.userTable.entity.Company;
import com.workhub.userTable.repository.CompanyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    private static final String COMPANY_NAME = "패스트캠퍼스";
    private static final String COMPANY_NUMBER = "1234567889";
    private static final String COMPANY_TEL = "02-123-4556";
    private static final String COMPANY_ADDRESS = "서울시 강남구 테헤란로 32";
    private static final Long COMPANY_ID = 1L;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    @Nested
    @DisplayName("registerCompany")
    class RegisterCompany {

        @Test
        @DisplayName("중복 없는 경우 고객사를 저장하고 DTO를 반환한다")
        void success() {
            CompanyRegisterRequest request = registerRequest();
            Company persistedCompany = persistedCompany();
            given(companyRepository.existsByCompanyNumber(request.companyNumber())).willReturn(false);
            given(companyRepository.save(any(Company.class))).willReturn(persistedCompany);

            CompanyResponse response = companyService.registerCompany(request);

            assertThat(response).isEqualTo(CompanyResponse.from(persistedCompany));

            ArgumentCaptor<Company> savedCompanyCaptor = ArgumentCaptor.forClass(Company.class);
            verify(companyRepository).existsByCompanyNumber(request.companyNumber());
            verify(companyRepository).save(savedCompanyCaptor.capture());

            Company savedCompany = savedCompanyCaptor.getValue();
            assertThat(savedCompany.getCompanyId()).isNull();
            assertThat(savedCompany.getCompanyName()).isEqualTo(request.companyName());
            assertThat(savedCompany.getCompanyNumber()).isEqualTo(request.companyNumber());
            assertThat(savedCompany.getTel()).isEqualTo(request.tel());
            assertThat(savedCompany.getAddress()).isEqualTo(request.address());
        }

        @Test
        @DisplayName("사업자 번호가 중복이면 예외가 발생한다")
        void fail_duplicateCompanyNumber() {
            CompanyRegisterRequest request = registerRequest();
            given(companyRepository.existsByCompanyNumber(request.companyNumber())).willReturn(true);

            assertThatThrownBy(() -> companyService.registerCompany(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.COMPANY_ALREADY_EXISTS.getMessage());

            verify(companyRepository).existsByCompanyNumber(request.companyNumber());
            verify(companyRepository, never()).save(any(Company.class));
        }
    }

    private CompanyRegisterRequest registerRequest() {
        return new CompanyRegisterRequest(
                COMPANY_NAME,
                COMPANY_NUMBER,
                COMPANY_TEL,
                COMPANY_ADDRESS
        );
    }

    private Company persistedCompany() {
        return Company.builder()
                .companyId(COMPANY_ID)
                .companyName(COMPANY_NAME)
                .companyNumber(COMPANY_NUMBER)
                .tel(COMPANY_TEL)
                .address(COMPANY_ADDRESS)
                .build();
    }
}
