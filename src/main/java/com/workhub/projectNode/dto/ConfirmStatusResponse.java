package com.workhub.projectNode.dto;

import com.workhub.projectNode.entity.ConfirmStatus;
import lombok.Builder;

@Builder
public record ConfirmStatusResponse(
        ConfirmStatus confirmStatus,
        String rejectText,
        String nodeTitle
) {
    public static ConfirmStatusResponse from(ConfirmStatus confirmStatus, String rejectText, String nodeTitle ) {

        return ConfirmStatusResponse.builder().confirmStatus(confirmStatus)
                .rejectText(rejectText).nodeTitle(nodeTitle).build();
    }
}
