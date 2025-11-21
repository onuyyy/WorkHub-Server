package com.workhub.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "link")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkId;

    @Column(name = "reference_link", nullable = false, length = 255)
    private String referenceLink;

    @Column(name = "link_description", nullable = true, length = 255)
    private String linkDescription;

    @Column(name = "post_id", nullable = false)
    private String post;
}
