package com.workhub.post.record.response;

import java.util.List;

/** 게시글 목록과 페이지 메타데이터를 함께 전달하는 DTO */
public record PostPageResponse(
        List<PostSummaryResponse> posts,
        long totalElements,
        int totalPages,
        int currentPage,
        int size
) {
    /** Page 객체에서 필요한 메타데이터만 추출한다. */
    public static PostPageResponse of(List<PostSummaryResponse> posts, org.springframework.data.domain.Page<?> page) {
        return new PostPageResponse(
                posts,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }
}
