package com.workhub.post.record.request;

import com.workhub.post.entity.PostType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostUpdateRequest (
        @NotBlank String title,
        @NotNull PostType postType,
        @NotBlank String content,
        @NotBlank String postIp,
        @Valid List<PostFileUpdateRequest> files,
        @Valid List<PostLinkUpdateRequest> links
) { }
