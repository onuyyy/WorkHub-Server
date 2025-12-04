package com.workhub.post.record.request;

import jakarta.validation.constraints.NotBlank;

public record PostFileRequest (
        @NotBlank String fileName,
        Integer fileOrder
){
}
