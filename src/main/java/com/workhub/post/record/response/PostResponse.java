package com.workhub.post.record.response;

import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostFile;
import com.workhub.post.entity.PostType;

import java.util.List;

public record PostResponse (
        Long postId,
        PostType postType,
        String title,
        String content,
        String postIp,
        Long parentPostId,
        List<PostFileResponse> files,
        List<PostLinkResponse> links
){
    public static PostResponse from(Post post, List<PostFile> postFiles, List<com.workhub.post.entity.PostLink> postLinks) {
        List<PostFileResponse> fileResponses = (postFiles == null) ? List.of()
                : postFiles.stream()
                .map(PostFileResponse::from)
                .toList();
        List<PostLinkResponse> linkResponses = (postLinks == null) ? List.of()
                : postLinks.stream()
                .map(PostLinkResponse::from)
                .toList();
        return new PostResponse(
                post.getPostId(),
                post.getType(),
                post.getTitle(),
                post.getContent(),
                post.getPostIp(),
                post.getParentPostId() != null ? post.getParentPostId() : null,
                fileResponses,
                linkResponses
        );
    }
}
