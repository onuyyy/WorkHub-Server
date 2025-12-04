package com.workhub.post.record.request;

import jakarta.validation.constraints.NotBlank;

public record PostLinkRequest(
        @NotBlank String referenceLink,
        @NotBlank String linkDescription
) {
}
