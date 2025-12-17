package com.workhub.cs.dto.csPost;

import jakarta.validation.constraints.NotBlank;

public record CsPostFileRequest(
        @NotBlank String fileName,
        @NotBlank String fileUrl,
        Integer fileOrder
) {
}
