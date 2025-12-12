package com.workhub.checklist.service.comment;

import com.workhub.checklist.dto.comment.CheckListCommentRequest;
import com.workhub.checklist.dto.comment.CheckListCommentResponse;
import com.workhub.checklist.entity.CheckList;
import com.workhub.checklist.entity.CheckListItem;
import com.workhub.checklist.entity.CheckListItemComment;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.util.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCheckListCommentServiceTest {

    @Mock
    private CheckListCommentService checkListCommentService;

    @Mock
    private CheckListAccessValidator checkListAccessValidator;

    @Mock
    private CheckListService checkListService;

    @InjectMocks
    private CreateCheckListCommentService createCheckListCommentService;

    private MockedStatic<SecurityUtil> securityUtil;

    @BeforeEach
    void setUp() {
        securityUtil = mockStatic(SecurityUtil.class);
        securityUtil.when(SecurityUtil::getCurrentUserIdOrThrow).thenReturn(88L);
    }

    @AfterEach
    void tearDown() {
        securityUtil.close();
    }

    @Test
    @DisplayName("체크리스트 댓글을 생성하면 저장된 정보가 반환된다")
    void givenValidRequest_whenCreate_thenReturnsResponse() {
        // given
        Long projectId = 1L;
        Long nodeId = 2L;
        Long checkListId = 3L;
        Long checkListItemId = 4L;
        String content = "댓글 내용";

        CheckList checkList = CheckList.builder()
                .checkListId(checkListId)
                .projectNodeId(nodeId)
                .userId(10L)
                .checkListDescription("desc")
                .build();

        CheckListItem checkListItem = CheckListItem.builder()
                .checkListItemId(checkListItemId)
                .checkListId(checkListId)
                .itemTitle("item")
                .itemOrder(1)
                .build();

        CheckListItemComment savedComment = CheckListItemComment.builder()
                .clCommentId(100L)
                .checkListItemId(checkListItemId)
                .userId(88L)
                .clContent(content)
                .build();

        CheckListCommentRequest request = CheckListCommentRequest.builder()
                .content(content)
                .patentClCommentId(null)
                .build();

        when(checkListService.findById(checkListId)).thenReturn(checkList);
        when(checkListService.findCheckListItem(checkListItemId)).thenReturn(checkListItem);
        when(checkListCommentService.save(any(CheckListItemComment.class))).thenReturn(savedComment);
        doNothing().when(checkListService)
                .snapShotAndRecordHistory(savedComment, savedComment.getClCommentId(), ActionType.CREATE);

        // when
        CheckListCommentResponse response =
                createCheckListCommentService.create(projectId, nodeId, checkListId, checkListItemId, request);

        // then
        assertThat(response.clCommentId()).isEqualTo(100L);
        assertThat(response.content()).isEqualTo(content);
        assertThat(response.userId()).isEqualTo(88L);

        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        verify(checkListAccessValidator).checkProjectMemberOrAdmin(projectId);
        verify(checkListCommentService).save(any(CheckListItemComment.class));
        verify(checkListService)
                .snapShotAndRecordHistory(savedComment, savedComment.getClCommentId(), ActionType.CREATE);
    }

    @Test
    @DisplayName("부모 댓글이 다른 항목이면 예외가 발생한다")
    void givenParentFromDifferentItem_whenCreate_thenThrowsException() {
        // given
        Long projectId = 11L;
        Long nodeId = 22L;
        Long checkListId = 33L;
        Long checkListItemId = 44L;
        Long parentId = 55L;

        CheckList checkList = CheckList.builder()
                .checkListId(checkListId)
                .projectNodeId(nodeId)
                .userId(1L)
                .checkListDescription("desc")
                .build();

        CheckListItem checkListItem = CheckListItem.builder()
                .checkListItemId(checkListItemId)
                .checkListId(checkListId)
                .itemTitle("item")
                .itemOrder(1)
                .build();

        CheckListItemComment parentComment = CheckListItemComment.builder()
                .clCommentId(parentId)
                .checkListItemId(999L)
                .userId(2L)
                .clContent("부모")
                .build();

        CheckListCommentRequest request = CheckListCommentRequest.builder()
                .content("child")
                .patentClCommentId(parentId)
                .build();

        when(checkListService.findById(checkListId)).thenReturn(checkList);
        when(checkListService.findCheckListItem(checkListItemId)).thenReturn(checkListItem);
        when(checkListCommentService.findById(parentId)).thenReturn(parentComment);

        // when & then
        assertThatThrownBy(() ->
                createCheckListCommentService.create(projectId, nodeId, checkListId, checkListItemId, request)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MATCHED_CHECK_LIST_ITEM_COMMENT);

        verify(checkListCommentService, never()).save(any(CheckListItemComment.class));
        verify(checkListService, never()).snapShotAndRecordHistory(any(CheckListItemComment.class), anyLong(), any());
    }

    @Test
    @DisplayName("댓글 내용이 비어있으면 예외가 발생한다")
    void givenBlankContent_whenCreate_thenThrowsException() {
        // given
        CheckListCommentRequest request = CheckListCommentRequest.builder()
                .content("   ")
                .patentClCommentId(null)
                .build();

        // when & then
        assertThatThrownBy(() ->
                createCheckListCommentService.create(1L, 2L, 3L, 4L, request)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CHECK_LIST_ITEM_COMMENT_CONTENT);

        verifyNoInteractions(checkListService);
        verify(checkListCommentService, never()).save(any());
    }
}
