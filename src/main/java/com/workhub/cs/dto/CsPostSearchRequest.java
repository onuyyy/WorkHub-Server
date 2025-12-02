package com.workhub.cs.dto;

import com.workhub.cs.entity.CsPostStatus;
import lombok.Builder;

@Builder
public record CsPostSearchRequest(
        String searchValue,
        CsPostStatus csPostStatus
) {
}
