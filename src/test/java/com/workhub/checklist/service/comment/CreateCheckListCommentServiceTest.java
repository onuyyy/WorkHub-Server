package com.workhub.checklist.service.comment;

import com.workhub.checklist.dto.comment.CheckListCommentFileRequest;
import com.workhub.checklist.dto.comment.CheckListCommentRequest;
import com.workhub.checklist.dto.comment.CheckListCommentResponse;
import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.comment.CheckListItemComment;
import com.workhub.checklist.entity.comment.CheckListItemCommentFile;
import com.workhub.checklist.event.CheckListCommentCreatedEvent;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
import com.workhub.file.dto.FileUploadResponse;
import com.workhub.file.service.FileService;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private FileService fileService;

    @InjectMocks
    private CreateCheckListCommentService createCheckListCommentService;

    private MockedStatic<SecurityUtil> securityUtil;

    @BeforeEach
    void setUp() {
        securityUtil = mockStatic(SecurityUtil.class);
        securityUtil.when(SecurityUtil::getCurrentUserIdOrThrow).thenReturn(88L);
        lenient().when(fileService.uploadFiles(any())).thenReturn(List.of());
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
        CheckListCommentResponse response = createCheckListCommentService.create(
                projectId,
                nodeId,
                checkListId,
                checkListItemId,
                request,
                null
        );

        // then
        assertThat(response.clCommentId()).isEqualTo(100L);
        assertThat(response.content()).isEqualTo(content);
        assertThat(response.userId()).isEqualTo(88L);

        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        verify(checkListAccessValidator).checkProjectMemberOrAdmin(projectId);
        verify(checkListCommentService).save(any(CheckListItemComment.class));
        verify(checkListService)
                .snapShotAndRecordHistory(savedComment, savedComment.getClCommentId(), ActionType.CREATE);
        verify(eventPublisher).publishEvent(any(CheckListCommentCreatedEvent.class));
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
        assertThatThrownBy(() -> createCheckListCommentService.create(
                projectId,
                nodeId,
                checkListId,
                checkListItemId,
                request,
                null
        ))
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
                .files(null)
                .build();

        // when & then
        assertThatThrownBy(() -> createCheckListCommentService.create(1L, 2L, 3L, 4L, request, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CHECK_LIST_ITEM_COMMENT_CONTENT);

        verifyNoInteractions(checkListService);
        verify(checkListCommentService, never()).save(any());
    }

    @Test
    @DisplayName("댓글 생성 시 파일 첨부가 포함되면 파일도 함께 저장된다")
    void givenRequestWithFiles_whenCreate_thenReturnResponseWithFiles() {
        // given
        Long projectId = 1L;
        Long nodeId = 2L;
        Long checkListId = 3L;
        Long checkListItemId = 4L;
        String content = "댓글 내용";
        List<CheckListCommentFileRequest> files = List.of(
                new CheckListCommentFileRequest("file1.jpg", 0),
                new CheckListCommentFileRequest("file2.png", 1)
        );

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
                .files(files)
                .build();

        when(checkListService.findById(checkListId)).thenReturn(checkList);
        when(checkListService.findCheckListItem(checkListItemId)).thenReturn(checkListItem);
        when(checkListCommentService.save(any(CheckListItemComment.class))).thenReturn(savedComment);
        when(checkListCommentService.saveCommentFile(any(CheckListItemCommentFile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(checkListService)
                .snapShotAndRecordHistory(savedComment, savedComment.getClCommentId(), ActionType.CREATE);

        // when
        List<MultipartFile> uploadFiles = List.of(
                new MockMultipartFile("files", "file1.jpg", "image/jpeg", new byte[]{1}),
                new MockMultipartFile("files", "file2.png", "image/png", new byte[]{2})
        );

        List<FileUploadResponse> uploadResponses = List.of(
                FileUploadResponse.from("stored-file1", "file1.jpg", ""),
                FileUploadResponse.from("stored-file2", "file2.png", "")
        );

        when(fileService.uploadFiles(uploadFiles)).thenReturn(uploadResponses);

        CheckListCommentResponse response = createCheckListCommentService.create(
                projectId,
                nodeId,
                checkListId,
                checkListItemId,
                request,
                uploadFiles
        );

        // then
        assertThat(response.clCommentId()).isEqualTo(100L);
        assertThat(response.content()).isEqualTo(content);
        assertThat(response.userId()).isEqualTo(88L);
        assertThat(response.files()).hasSize(2);
        assertThat(response.files().get(0).fileName()).isEqualTo("file1.jpg");
        assertThat(response.files().get(1).fileName()).isEqualTo("file2.png");
        assertThat(response.files().get(0).fileOrder()).isEqualTo(0);
        assertThat(response.files().get(1).fileOrder()).isEqualTo(1);

        verify(checkListCommentService, times(2)).saveCommentFile(any(CheckListItemCommentFile.class));
        verify(fileService).uploadFiles(uploadFiles);
    }

    @Test
    @DisplayName("파일 첨부 없이 댓글을 생성하면 빈 파일 리스트가 반환된다")
    void givenRequestWithoutFiles_whenCreate_thenReturnResponseWithEmptyFiles() {
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
                .files(null)
                .build();

        when(checkListService.findById(checkListId)).thenReturn(checkList);
        when(checkListService.findCheckListItem(checkListItemId)).thenReturn(checkListItem);
        when(checkListCommentService.save(any(CheckListItemComment.class))).thenReturn(savedComment);
        doNothing().when(checkListService)
                .snapShotAndRecordHistory(savedComment, savedComment.getClCommentId(), ActionType.CREATE);

        // when
        CheckListCommentResponse response = createCheckListCommentService.create(
                projectId,
                nodeId,
                checkListId,
                checkListItemId,
                request,
                null
        );

        // then
        assertThat(response.clCommentId()).isEqualTo(100L);
        assertThat(response.content()).isEqualTo(content);
        assertThat(response.files()).isEmpty();

        verify(checkListCommentService, never()).saveCommentFile(any(CheckListItemCommentFile.class));
    }
}
