package com.workhub.checklist.controller;

import com.workhub.checklist.api.CheckListCommentApi;
import com.workhub.checklist.dto.comment.CheckListCommentRequest;
import com.workhub.checklist.dto.comment.CheckListCommentResponse;
import com.workhub.checklist.service.comment.CreateCheckListCommentService;
import com.workhub.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/nodes/{nodeId}/checkLists")
public class CheckListCommentController implements CheckListCommentApi {

    private final CreateCheckListCommentService createCheckListCommentService;

    @Override
    @PostMapping("/{checkListId}/items/{checkListItemId}/comments")
    public ResponseEntity<ApiResponse<CheckListCommentResponse>> create(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long checkListId,
            @PathVariable Long checkListItemId,
            @Valid @RequestBody CheckListCommentRequest request

    ) {

        CheckListCommentResponse response =
                createCheckListCommentService.create(projectId, nodeId, checkListId, checkListItemId, request);

        return ApiResponse.created(response, "체크리스트 댓글이 작성되었습니다.");
    }



}
