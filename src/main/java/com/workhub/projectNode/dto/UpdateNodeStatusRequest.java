package com.workhub.projectNode.dto;

import com.workhub.projectNode.entity.NodeStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateNodeStatusRequest(
        @NotNull
        NodeStatus nodeStatus
) {
}
