package com.workhub.post.dto.post.request;

import jakarta.validation.constraints.NotBlank;

public record PostFileRequest (
        @NotBlank String fileName,
        Integer fileOrder
){
}
