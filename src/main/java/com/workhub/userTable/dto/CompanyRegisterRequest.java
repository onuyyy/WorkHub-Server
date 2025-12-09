package com.workhub.userTable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRegisterRequest(
        @NotBlank(message = "고객사 이름은 필수 값입니다.")
        @Size(max = 50, message = "고객사 이름은 50자 이하로 입력해주세요.")
        String companyName,

        @NotBlank(message = "사업자 등록 번호는 필수 값입니다.")
        @Size(max = 20, message = "사업자 등록 번호는 20자 이하로 입력해주세요.")
        String companyNumber,

        @NotBlank(message = "연락처는 필수 값입니다.")
        @Size(max = 20, message = "연락처는 20자 이하로 입력해주세요.")
        String tel,

        @NotBlank(message = "주소는 필수 값입니다.")
        @Size(max = 100, message = "주소는 100자 이하로 입력해주세요.")
        String address
) {
}
