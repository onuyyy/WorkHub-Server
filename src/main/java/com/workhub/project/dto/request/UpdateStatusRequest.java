package com.workhub.project.dto.request;

import com.workhub.project.entity.Status;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull
        Status status
) {
}
