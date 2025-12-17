package com.workhub.projectNode.dto;

import com.workhub.projectNode.entity.ConfirmStatus;

public record ClientStatusRequest(
        ConfirmStatus confirmStatus,
        String rejectMessage
) {
}
