package com.workhub.project.dto;

import com.workhub.project.entity.Status;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull
        Status status
) {
}
