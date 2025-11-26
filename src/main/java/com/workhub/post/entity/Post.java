package com.workhub.post.entity;

import com.workhub.global.entity.BaseTimeEntity;
import com.workhub.post.record.request.PostRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "post")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PostType type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "hashtag", nullable = false)
    private HashTag hashtag;

    @Column(name = "post_ip")
    private String postIp;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "project_node_id")
    private String projectNodeId;

    @ManyToOne
    @JoinColumn(name = "parent_post_id")
    private Post parentPostId;

    @OneToMany(mappedBy = "parentPostId")
    private List<Post> posts;

    public static Post of(Post parent, PostRequest request) {
        return Post.builder()
                .type(request.postType())
                .title(request.title())
                .content(request.content())
                .postIp(request.postIp())
                .hashtag(request.hashTag())
                .parentPostId(parent)
                .build();
    }
}
