package com.workhub.post.dto.post.request;

import jakarta.validation.constraints.NotBlank;

public record PostLinkUpdateRequest(
        Long linkId,
        @NotBlank String referenceLink,
        @NotBlank String linkDescription,
        boolean deleted
) {
}
