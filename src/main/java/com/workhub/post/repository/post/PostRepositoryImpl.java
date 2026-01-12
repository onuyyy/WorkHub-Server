package com.workhub.post.repository.post;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import com.workhub.post.entity.QPost;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    @Override
    public Page<Post> searchParentPosts(Long nodeId, String keyword, PostType postType, Pageable pageable) {
        QPost post = QPost.post;
        BooleanBuilder where = new BooleanBuilder();
        where.and(post.deletedAt.isNull());
        where.and(post.projectNodeId.eq(nodeId));
        where.and(post.parentPostId.isNull());
        if (keyword != null && !keyword.isBlank()) {
            where.and(post.title.containsIgnoreCase(keyword)
                    .or(post.content.containsIgnoreCase(keyword)));
        }
        if (postType != null) {
            where.and(post.type.eq(postType));
        }

        List<Post> content = queryFactory
                .selectFrom(post)
                .where(where)
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}


