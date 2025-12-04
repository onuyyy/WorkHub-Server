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
        List<PostFileResponse> files
){
    public static PostResponse from(Post post, List<PostFile> postFiles) {
        List<PostFileResponse> fileResponses = (postFiles == null) ? List.of()
                : postFiles.stream()
                .map(PostFileResponse::from)
                .toList();
        return new PostResponse(
                post.getPostId(),
                post.getType(),
                post.getTitle(),
                post.getContent(),
                post.getPostIp(),
                post.getParentPostId() != null ? post.getParentPostId() : null,
                fileResponses
        );
    }
}
