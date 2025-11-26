package com.workhub.post.record.request;

import com.workhub.post.entity.HashTag;
import com.workhub.post.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostUpdateRequest (
        @NotBlank String title,
        @NotNull PostType postType,
        @NotBlank String content,
        @NotBlank String postIp,
        @NotNull HashTag hashTag
) { }
