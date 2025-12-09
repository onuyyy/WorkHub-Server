package com.workhub.post.repository.comment;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.workhub.post.entity.PostComment;
import com.workhub.post.entity.QPostComment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostComment> findByPostWithReplies(Long postId, Pageable pageable) {
        QPostComment postComment = QPostComment.postComment;

        // 최상위 댓글만 페이징하여 조회
        List<PostComment> topLevelComments = queryFactory
                .selectFrom(postComment)
                .where(
                        postIdEq(postId),
                        postComment.parentCommentId.isNull()
                )
                .orderBy(postComment.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 최상위 댓글의 총 개수
        Long total = queryFactory
                .select(postComment.count())
                .from(postComment)
                .where(
                        postIdEq(postId),
                        postComment.parentCommentId.isNull()
                )
                .fetchOne();

        return new PageImpl<>(topLevelComments, pageable, total == null ? 0 : total);
    }

    @Override
    public List<PostComment> findAllByPostId(Long postId) {
        QPostComment postComment = QPostComment.postComment;

        return queryFactory
                .selectFrom(postComment)
                .where(postIdEq(postId))
                .orderBy(postComment.createdAt.asc())
                .fetch();
    }

    private BooleanExpression postIdEq(Long postId) {
        return postId == null ? null : QPostComment.postComment.postId.eq(postId);
    }
}
