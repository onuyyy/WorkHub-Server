package com.workhub.post.record.response;

import com.workhub.post.entity.PostLink;

public record PostLinkResponse(
        Long linkId,
        String referenceLink,
        String linkDescription
) {
    public static PostLinkResponse from(PostLink link) {
        return new PostLinkResponse(
                link.getLinkId(),
                link.getReferenceLink(),
                link.getLinkDescription()
        );
    }
}
