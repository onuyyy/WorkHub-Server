package com.workhub.checklist.service.comment;

import com.workhub.checklist.dto.comment.CheckListCommentResponse;
import com.workhub.checklist.dto.comment.CheckListCommentUpdateRequest;
import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.comment.CheckListItemComment;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
import com.workhub.file.service.FileService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.port.AuthorLookupPort;
import com.workhub.global.port.dto.AuthorProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCheckListCommentServiceTest {

    @Mock
    private CheckListCommentService checkListCommentService;

    @Mock
    private CheckListAccessValidator checkListAccessValidator;

    @Mock
    private CheckListService checkListService;

    @Mock
    private FileService fileService;

    @Mock
    private AuthorLookupPort authorLookupPort;

    @InjectMocks
    private UpdateCheckListCommentService updateCheckListCommentService;

    @BeforeEach
    void setUp() {
        lenient().when(fileService.uploadFiles(any())).thenReturn(List.of());
        lenient().when(checkListCommentService.findCommentFilesByCommentId(anyLong())).thenReturn(List.of());
        lenient().when(authorLookupPort.findByUserIds(any())).thenAnswer(invocation -> {
            List<Long> userIds = invocation.getArgument(0);
            Map<Long, AuthorProfile> result = new HashMap<>();
            if (userIds != null) {
                for (Long id : userIds) {
                    result.put(id, new AuthorProfile(id, "user-" + id));
                }
            }
            return result;
        });
    }

    @Test
    @DisplayName("체크리스트 댓글을 수정하면 최신 내용이 반환된다")
    void givenValidRequest_whenUpdate_thenReturnUpdatedComment() {
        // given
        Long projectId = 1L;
        Long nodeId = 2L;
        Long checkListId = 3L;
        Long checkListItemId = 4L;
        Long commentId = 5L;

        CheckList checkList = CheckList.builder()
                .checkListId(checkListId)
                .projectNodeId(nodeId)
                .userId(10L)
                .checkListDescription("desc")
                .build();

        CheckListItem checkListItem = CheckListItem.builder()
                .checkListItemId(checkListItemId)
                .checkListId(checkListId)
                .itemTitle("title")
                .itemOrder(1)
                .build();

        CheckListItemComment comment = CheckListItemComment.builder()
                .clCommentId(commentId)
                .checkListItemId(checkListItemId)
                .userId(88L)
                .clContent("old")
                .build();

        CheckListCommentUpdateRequest request = CheckListCommentUpdateRequest.builder()
                .content("updated")
                .build();

        when(checkListService.findById(checkListId)).thenReturn(checkList);
        when(checkListService.findCheckListItem(checkListItemId)).thenReturn(checkListItem);
        when(checkListCommentService.findById(commentId)).thenReturn(comment);

        // when
        CheckListCommentResponse response = updateCheckListCommentService.update(
                projectId, nodeId, checkListId, checkListItemId, commentId, request, null);

        // then
        assertThat(response.content()).isEqualTo("updated");
        assertThat(response.clCommentId()).isEqualTo(commentId);
        assertThat(response.userName()).isEqualTo("user-88");

        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        verify(checkListAccessValidator).checkProjectMemberOrAdmin(projectId);
        verify(checkListAccessValidator).validateAdminOrCommentOwner(comment.getUserId());
        verify(checkListService).snapShotAndRecordHistory(comment, comment.getClCommentId(), ActionType.UPDATE);
    }

    @Test
    @DisplayName("작성자가 아니면 댓글을 수정할 수 없다")
    void givenNotOwningUser_whenUpdate_thenThrowsException() {
        // given
        Long projectId = 1L;
        Long nodeId = 2L;
        Long checkListId = 3L;
        Long checkListItemId = 4L;
        Long commentId = 5L;

        CheckList checkList = CheckList.builder()
                .checkListId(checkListId)
                .projectNodeId(nodeId)
                .userId(10L)
                .checkListDescription("desc")
                .build();

        CheckListItem checkListItem = CheckListItem.builder()
                .checkListItemId(checkListItemId)
                .checkListId(checkListId)
                .itemTitle("title")
                .itemOrder(1)
                .build();

        CheckListItemComment comment = CheckListItemComment.builder()
                .clCommentId(commentId)
                .checkListItemId(checkListItemId)
                .userId(999L)
                .clContent("old")
                .build();

        CheckListCommentUpdateRequest request = CheckListCommentUpdateRequest.builder()
                .content("updated")
                .build();

        when(checkListService.findById(checkListId)).thenReturn(checkList);
        when(checkListService.findCheckListItem(checkListItemId)).thenReturn(checkListItem);
        when(checkListCommentService.findById(commentId)).thenReturn(comment);
        doThrow(new BusinessException(ErrorCode.NOT_AUTHORIZED_CHECK_LIST_ITEM_COMMENT_USER))
                .when(checkListAccessValidator).validateAdminOrCommentOwner(comment.getUserId());

        // when & then
        assertThatThrownBy(() -> updateCheckListCommentService.update(
                projectId, nodeId, checkListId, checkListItemId, commentId, request, null
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_AUTHORIZED_CHECK_LIST_ITEM_COMMENT_USER);

        verify(checkListAccessValidator).validateAdminOrCommentOwner(comment.getUserId());
        verify(checkListService, never()).snapShotAndRecordHistory(any(CheckListItemComment.class), anyLong(), any(ActionType.class));
    }

    @Test
    @DisplayName("어드민은 작성자가 아니어도 댓글을 수정할 수 있다")
    void givenAdminUser_whenUpdate_thenSuccessEvenNotOwner() {
        // given
        Long projectId = 1L;
        Long nodeId = 2L;
        Long checkListId = 3L;
        Long checkListItemId = 4L;
        Long commentId = 5L;

        CheckList checkList = CheckList.builder()
                .checkListId(checkListId)
                .projectNodeId(nodeId)
                .userId(10L)
                .checkListDescription("desc")
                .build();

        CheckListItem checkListItem = CheckListItem.builder()
                .checkListItemId(checkListItemId)
                .checkListId(checkListId)
                .itemTitle("title")
                .itemOrder(1)
                .build();

        CheckListItemComment comment = CheckListItemComment.builder()
                .clCommentId(commentId)
                .checkListItemId(checkListItemId)
                .userId(999L)
                .clContent("old")
                .build();

        CheckListCommentUpdateRequest request = CheckListCommentUpdateRequest.builder()
                .content("updated")
                .build();

        when(checkListService.findById(checkListId)).thenReturn(checkList);
        when(checkListService.findCheckListItem(checkListItemId)).thenReturn(checkListItem);
        when(checkListCommentService.findById(commentId)).thenReturn(comment);

        // when
        CheckListCommentResponse response = updateCheckListCommentService.update(
                projectId, nodeId, checkListId, checkListItemId, commentId, request, null);

        // then
        assertThat(response.content()).isEqualTo("updated");
        assertThat(response.userName()).isEqualTo("user-999");
        verify(checkListAccessValidator).validateAdminOrCommentOwner(comment.getUserId());
        verify(checkListService).snapShotAndRecordHistory(comment, comment.getClCommentId(), ActionType.UPDATE);
    }

    @Test
    @DisplayName("수정 본문이 비어있으면 예외가 발생한다")
    void givenBlankContent_whenUpdate_thenThrowsException() {
        // given
        CheckListCommentUpdateRequest request = CheckListCommentUpdateRequest.builder()
                .content("   ")
                .build();

        // when & then
        assertThatThrownBy(() -> updateCheckListCommentService.update(1L, 2L, 3L, 4L, 5L, request, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CHECK_LIST_ITEM_COMMENT_CONTENT);

        verifyNoInteractions(checkListService);
        verify(checkListAccessValidator, never()).validateAdminOrCommentOwner(any());
    }

    @Test
    @DisplayName("댓글이 요청한 항목에 속하지 않으면 수정할 수 없다")
    void givenCommentNotBelongingItem_whenUpdate_thenThrowsException() {
        // given
        Long projectId = 1L;
        Long nodeId = 2L;
        Long checkListId = 3L;
        Long checkListItemId = 4L;
        Long commentId = 5L;

        CheckList checkList = CheckList.builder()
                .checkListId(checkListId)
                .projectNodeId(nodeId)
                .userId(10L)
                .checkListDescription("desc")
                .build();

        CheckListItem checkListItem = CheckListItem.builder()
                .checkListItemId(checkListItemId)
                .checkListId(checkListId)
                .itemTitle("title")
                .itemOrder(1)
                .build();

        CheckListItemComment comment = CheckListItemComment.builder()
                .clCommentId(commentId)
                .checkListItemId(999L)
                .userId(88L)
                .clContent("old")
                .build();

        CheckListCommentUpdateRequest request = CheckListCommentUpdateRequest.builder()
                .content("new")
                .build();

        when(checkListService.findById(checkListId)).thenReturn(checkList);
        when(checkListService.findCheckListItem(checkListItemId)).thenReturn(checkListItem);
        when(checkListCommentService.findById(commentId)).thenReturn(comment);

        // when & then
        assertThatThrownBy(() -> updateCheckListCommentService.update(
                projectId, nodeId, checkListId, checkListItemId, commentId, request, null
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MATCHED_CHECK_LIST_ITEM_COMMENT);

        verify(checkListAccessValidator, never()).validateAdminOrCommentOwner(any());
        verify(checkListService, never()).snapShotAndRecordHistory(any(CheckListItemComment.class), anyLong(), any(ActionType.class));
    }
}
