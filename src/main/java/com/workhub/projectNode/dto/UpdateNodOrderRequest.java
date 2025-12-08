package com.workhub.projectNode.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateNodOrderRequest(

        @NotNull
        Long projectNodeId,
        @NotNull
        Integer nodeOrder
) {
}
