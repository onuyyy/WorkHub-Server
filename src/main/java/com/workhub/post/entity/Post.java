package com.workhub.post.entity;

import com.workhub.global.entity.BaseTimeEntity;
import com.workhub.post.record.request.PostRequest;
import com.workhub.post.record.request.PostUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "post")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE post SET deleted_at  = CURRENT_TIMESTAMP WHERE post_id = ?")
@SQLRestriction("deleted_at is NULL")
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PostType type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "hashtag")
    private HashTag hashtag;

    @Column(name = "post_ip", length = 20)
    private String postIp;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "project_node_id")
    private Long projectNodeId;

    @Column(name = "parent_post_id")
    private Long parentPostId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Post of(Long parentPostId, PostRequest request) {
        return Post.builder()
                .type(request.postType())
                .title(request.title())
                .content(request.content())
                .postIp(request.postIp())
                .hashtag(request.hashTag())
                .parentPostId(parentPostId)
                .build();
    }

    public void update(PostUpdateRequest request) {
        this.title = request.title();
        this.content = request.content();
        this.type = request.postType();
        this.postIp = request.postIp();
        this.hashtag = request.hashTag();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}
