package com.workhub.post.repository;

import com.workhub.post.entity.HashTag;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import org.springframework.data.jpa.domain.Specification;

public final class PostSpecifications {

    private PostSpecifications() {
    }

    /** 특정 프로젝트 노드에 속한 게시글만 필터링한다. */
    public static Specification<Post> withProjectNode(Long nodeId) {
        return (root, query, builder) -> nodeId == null ? null : builder.equal(root.get("projectNodeId"), nodeId);
    }

    /** 제목/내용에 키워드가 포함된 게시글만 필터링한다. */
    public static Specification<Post> withKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return (root, query, builder) -> {
            String likeKeyword = "%" + keyword.trim() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("title")), likeKeyword.toLowerCase()),
                    builder.like(builder.lower(root.get("content")), likeKeyword.toLowerCase())
            );
        };
    }

    /** 게시글 타입 일치 여부를 필터링한다. */
    public static Specification<Post> withPostType(PostType postType) {
        return postType == null ? null : (root, query, builder) -> builder.equal(root.get("type"), postType);
    }

    /** 해시태그 일치 여부를 필터링한다. */
    public static Specification<Post> withHashTag(HashTag hashTag) {
        return hashTag == null ? null : (root, query, builder) -> builder.equal(root.get("hashtag"), hashTag);
    }
}
