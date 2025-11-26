package com.workhub.cs.dto;

import jakarta.validation.constraints.NotBlank;

public record CsPostFileRequest(
        @NotBlank String fileUrl,
        @NotBlank String fileName,
        Integer fileOrder
) {
}