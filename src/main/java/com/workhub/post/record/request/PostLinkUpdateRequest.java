package com.workhub.post.record.request;

import jakarta.validation.constraints.NotBlank;

public record PostLinkUpdateRequest(
        Long linkId,
        @NotBlank String referenceLink,
        @NotBlank String linkDescription,
        boolean deleted
) {
}
