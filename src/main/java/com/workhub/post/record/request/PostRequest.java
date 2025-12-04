package com.workhub.post.record.request;

import com.workhub.post.entity.HashTag;
import com.workhub.post.entity.PostType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostRequest(
        @NotBlank String title,
        @NotNull PostType postType,
        @NotBlank String content,
        @NotBlank String postIp,
        Long parentPostId,
        HashTag hashTag,

        @Valid List<PostFileRequest> files,
        @Valid List<PostLinkRequest> links
) { }
