package com.workhub.projectNode.dto;

import com.workhub.projectNode.entity.ConfirmStatus;
import lombok.Builder;

@Builder
public record ConfirmStatusResponse(
        ConfirmStatus confirmStatus,
        String rejectText
) {
    public static ConfirmStatusResponse from(ConfirmStatus confirmStatus, String rejectText) {

        return ConfirmStatusResponse.builder().confirmStatus(confirmStatus).rejectText(rejectText).build();
    }
}
