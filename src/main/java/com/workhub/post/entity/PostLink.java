package com.workhub.post.entity;

import com.workhub.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "post_link")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PostLink extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "reference_link", length = 255)
    private String referenceLink;

    @Column(name = "link_description", length = 255)
    private String linkDescription;

    @Column(name = "post_id")
    private Long postId;

    public static PostLink of(Long postId, String referenceLink, String linkDescription) {
        return PostLink.builder()
                .postId(postId)
                .referenceLink(referenceLink)
                .linkDescription(linkDescription)
                .build();
    }

    public void update(String referenceLink, String linkDescription) {
        this.referenceLink = referenceLink;
        this.linkDescription = linkDescription;
    }

    public void markDeleted() {
        markDeletedNow();
    }
}
