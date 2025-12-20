package com.workhub.checklist.service.comment;

import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.comment.CheckListItemComment;
import com.workhub.checklist.entity.comment.CheckListItemCommentFile;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
import com.workhub.file.service.FileService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCheckListCommentServiceTest {

    @Mock
    private CheckListCommentService checkListCommentService;

    @Mock
    private CheckListAccessValidator checkListAccessValidator;

    @Mock
    private CheckListService checkListService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private DeleteCheckListCommentService deleteCheckListCommentService;

    @Test
    @DisplayName("댓글 삭제 시 대댓글과 첨부 파일까지 모두 삭제된다")
    void givenValidRequest_whenDelete_thenCascadeSoftDelete() {
        // given
        Long projectId = 1L;
        Long nodeId = 2L;
        Long checkListId = 3L;
        Long itemId = 4L;
        Long parentCommentId = 5L;
        Long childCommentId = 6L;
        Long grandChildCommentId = 7L;

        CheckList checkList = CheckList.builder()
                .checkListId(checkListId)
                .projectNodeId(nodeId)
                .userId(11L)
                .checkListDescription("desc")
                .build();

        CheckListItem checkListItem = CheckListItem.builder()
                .checkListItemId(itemId)
                .checkListId(checkListId)
                .itemTitle("title")
                .itemOrder(1)
                .build();

        CheckListItemComment parent = CheckListItemComment.builder()
                .clCommentId(parentCommentId)
                .checkListItemId(itemId)
                .userId(100L)
                .clContent("parent")
                .build();

        CheckListItemComment child = CheckListItemComment.builder()
                .clCommentId(childCommentId)
                .checkListItemId(itemId)
                .parentClCommentId(parentCommentId)
                .userId(101L)
                .clContent("child")
                .build();

        CheckListItemComment grandChild = CheckListItemComment.builder()
                .clCommentId(grandChildCommentId)
                .checkListItemId(itemId)
                .parentClCommentId(childCommentId)
                .userId(102L)
                .clContent("grand")
                .build();

        List<CheckListItemCommentFile> parentFiles = List.of(
                CheckListItemCommentFile.builder()
                        .commentFileId(1L)
                        .clCommentId(parentCommentId)
                        .fileUrl("parent-managed")
                        .fileName("parent.png")
                        .fileOrder(1)
                        .build(),
                CheckListItemCommentFile.builder()
                        .commentFileId(2L)
                        .clCommentId(parentCommentId)
                        .fileName("local.txt")
                        .fileOrder(2)
                        .build()
        );

        List<CheckListItemCommentFile> childFiles = List.of(
                CheckListItemCommentFile.builder()
                        .commentFileId(3L)
                        .clCommentId(childCommentId)
                        .fileUrl("child-managed")
                        .fileName("child.png")
                        .fileOrder(1)
                        .build()
        );

        when(checkListService.findById(checkListId)).thenReturn(checkList);
        when(checkListService.findCheckListItem(itemId)).thenReturn(checkListItem);
        when(checkListCommentService.findById(parentCommentId)).thenReturn(parent);
        when(checkListCommentService.findChildrenByParentId(parentCommentId)).thenReturn(List.of(child));
        when(checkListCommentService.findChildrenByParentId(childCommentId)).thenReturn(List.of(grandChild));
        when(checkListCommentService.findChildrenByParentId(grandChildCommentId)).thenReturn(List.of());
        when(checkListCommentService.findCommentFilesByCommentId(parentCommentId)).thenReturn(parentFiles);
        when(checkListCommentService.findCommentFilesByCommentId(childCommentId)).thenReturn(childFiles);
        when(checkListCommentService.findCommentFilesByCommentId(grandChildCommentId)).thenReturn(List.of());
        doNothing().when(checkListCommentService).deleteCommentFiles(any());

        // when
        Long deletedId = deleteCheckListCommentService.delete(
                projectId, nodeId, checkListId, itemId, parentCommentId);

        // then
        assertThat(deletedId).isEqualTo(parentCommentId);
        assertThat(parent.isDeleted()).isTrue();
        assertThat(child.isDeleted()).isTrue();
        assertThat(grandChild.isDeleted()).isTrue();

        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        verify(checkListAccessValidator).checkProjectMemberOrAdmin(projectId);
        verify(checkListAccessValidator).validateAdminOrCommentOwner(parent.getUserId());

        verify(checkListService).snapShotAndRecordHistory(parent, parentCommentId, ActionType.DELETE);
        verify(checkListService).snapShotAndRecordHistory(child, childCommentId, ActionType.DELETE);
        verify(checkListService).snapShotAndRecordHistory(grandChild, grandChildCommentId, ActionType.DELETE);

        verify(checkListCommentService).deleteCommentFiles(parentFiles);
        verify(checkListCommentService).deleteCommentFiles(childFiles);

        ArgumentCaptor<List<String>> fileCaptor = ArgumentCaptor.forClass(List.class);
        verify(fileService).deleteFiles(fileCaptor.capture());
        assertThat(fileCaptor.getValue())
                .containsExactlyInAnyOrder("parent-managed", "child-managed");
    }

    @Test
    @DisplayName("이미 삭제된 댓글은 다시 삭제할 수 없다")
    void givenDeletedComment_whenDelete_thenThrowsException() {
        // given
        Long projectId = 1L;
        Long nodeId = 2L;
        Long checkListId = 3L;
        Long itemId = 4L;
        Long commentId = 5L;

        CheckList checkList = CheckList.builder()
                .checkListId(checkListId)
                .projectNodeId(nodeId)
                .userId(11L)
                .checkListDescription("desc")
                .build();

        CheckListItem checkListItem = CheckListItem.builder()
                .checkListItemId(itemId)
                .checkListId(checkListId)
                .itemTitle("title")
                .itemOrder(1)
                .build();

        CheckListItemComment comment = CheckListItemComment.builder()
                .clCommentId(commentId)
                .checkListItemId(itemId)
                .userId(100L)
                .clContent("content")
                .build();
        comment.markDeleted();

        when(checkListService.findById(checkListId)).thenReturn(checkList);
        when(checkListService.findCheckListItem(itemId)).thenReturn(checkListItem);
        when(checkListCommentService.findById(commentId)).thenReturn(comment);

        // when & then
        assertThatThrownBy(() -> deleteCheckListCommentService.delete(
                projectId, nodeId, checkListId, itemId, commentId
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_DELETED_CHECK_LIST_ITEM_COMMENT);

        verify(checkListAccessValidator, never()).validateAdminOrCommentOwner(anyLong());
        verify(checkListCommentService, never()).findChildrenByParentId(anyLong());
        verify(checkListCommentService, never()).deleteCommentFiles(any());
        verify(fileService, never()).deleteFiles(any());
    }
}
