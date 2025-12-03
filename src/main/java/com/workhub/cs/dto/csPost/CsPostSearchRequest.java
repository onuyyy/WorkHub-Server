package com.workhub.cs.dto.csPost;

import com.workhub.cs.entity.CsPostStatus;
import lombok.Builder;

@Builder
public record CsPostSearchRequest(
        String searchValue,
        CsPostStatus csPostStatus
) {
}
