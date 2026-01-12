package com.workhub.post.dto.post.request;

import jakarta.validation.constraints.NotBlank;

public record PostLinkRequest(
        @NotBlank String referenceLink,
        @NotBlank String linkDescription
) {
}
