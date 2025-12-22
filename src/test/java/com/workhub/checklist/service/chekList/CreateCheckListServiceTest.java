package com.workhub.checklist.service.chekList;

import com.workhub.checklist.dto.checkList.*;
import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.checkList.CheckListOption;
import com.workhub.checklist.entity.checkList.CheckListOptionFile;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
import com.workhub.checklist.service.checkList.CreateCheckListService;
import com.workhub.checklist.service.checkList.CreateCheckListTemplateService;
import com.workhub.file.dto.FileUploadResponse;
import com.workhub.file.service.FileService;
import com.workhub.checklist.event.CheckListCreatedEvent;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCheckListServiceTest {

    @Mock
    private CheckListService checkListService;

    @Mock
    private CheckListAccessValidator checkListAccessValidator;

    @Mock
    private FileService fileService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CreateCheckListTemplateService createCheckListTemplateService;

    @InjectMocks
    private CreateCheckListService createCheckListService;

    private CheckList mockCheckList;
    private CheckListItem mockItem1;
    private CheckListItem mockItem2;
    private CheckListOption mockOption1;
    private CheckListOption mockOption2;
    private CheckListOptionFile mockFile1;
    private CheckListUserInfo ownerInfo;

    @BeforeEach
    void init() {
        mockCheckList = CheckList.builder()
                .checkListId(1L)
                .checkListDescription("전달사항")
                .projectNodeId(10L)
                .userId(2L)
                .build();

        mockItem1 = CheckListItem.builder()
                .checkListItemId(1L)
                .itemTitle("항목1")
                .itemOrder(1)
                .checkListId(1L)
                .userId(2L)
                .status(CheckListItemStatus.PENDING)
                .build();

        mockItem2 = CheckListItem.builder()
                .checkListItemId(2L)
                .itemTitle("항목2")
                .itemOrder(2)
                .checkListId(1L)
                .userId(2L)
                .status(CheckListItemStatus.PENDING)
                .build();

        mockOption1 = CheckListOption.builder()
                .checkListOptionId(1L)
                .optionContent("선택지1")
                .optionOrder(1)
                .checkListItemId(1L)
                .build();

        mockOption2 = CheckListOption.builder()
                .checkListOptionId(2L)
                .optionContent("선택지2")
                .optionOrder(2)
                .checkListItemId(1L)
                .build();

        mockFile1 = CheckListOptionFile.builder()
                .checkListOptionFileId(1L)
                .fileUrl("https://example.com/file1.png")
                .fileName("file1.png")
                .fileOrder(0)
                .checkListOptionId(1L)
                .build();

        ownerInfo = CheckListUserInfo.of("담당자", "010-1111-1111");
        lenient().when(checkListService.resolveUserInfo(anyLong())).thenReturn(ownerInfo);
        lenient().when(fileService.uploadFiles(any())).thenReturn(List.of());
        lenient().doNothing().when(checkListAccessValidator).checkProjectDevMemberOrAdmin(anyLong());
        lenient().doNothing().when(checkListService).existNodeCheck(anyLong());
    }

    @Test
    @DisplayName("체크리스트를 생성하면 생성한 체크리스트 정보를 보여준다.")
    void givenCheckListCreateRequest_whenCreate_thenSuccess() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, null);
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = createRequest(List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);

        // when
        CheckListResponse result = createCheckListService.create(projectId, nodeId, userId, request, List.of());

        // then
        assertThat(result.checkListId()).isEqualTo(1L);
        assertThat(result.description()).isEqualTo("전달사항");
        assertThat(result.projectNodeId()).isEqualTo(10L);
        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.items()).hasSize(1);

        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        verify(checkListAccessValidator).checkProjectDevMemberOrAdmin(projectId);
        verify(checkListService).existNodeCheck(nodeId);
        verify(checkListService).saveCheckList(any(CheckList.class));
        verify(checkListService).saveCheckListItem(any(CheckListItem.class));
        verify(checkListService).saveCheckListOption(any(CheckListOption.class));
        verify(eventPublisher).publishEvent(any(CheckListCreatedEvent.class));
    }

    @Test
    @DisplayName("파일이 포함된 옵션 생성 시 파일도 저장된다.")
    void givenRequestWithFiles_whenCreate_thenFilesAreSaved() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        List<String> fileUrls = Arrays.asList("https://example.com/file1.png", "https://example.com/file2.png");
        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, fileUrls);
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = createRequest(List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);
        when(checkListService.saveCheckListOptionFile(any(CheckListOptionFile.class))).thenReturn(mockFile1);

        // when
        createCheckListService.create(projectId, nodeId, userId, request, List.of());

        // then
        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        verify(checkListAccessValidator).checkProjectDevMemberOrAdmin(projectId);
        verify(checkListService).existNodeCheck(nodeId);
        verify(checkListService).saveCheckList(any(CheckList.class));
        verify(checkListService).saveCheckListItem(any(CheckListItem.class));
        verify(checkListService).saveCheckListOption(any(CheckListOption.class));
        verify(checkListService, times(2)).saveCheckListOptionFile(any(CheckListOptionFile.class));
    }

    @Test
    @DisplayName("여러 항목과 옵션이 있는 체크리스트를 생성할 수 있다.")
    void givenMultipleItemsAndOptions_whenCreate_thenAllSaved() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        CheckListOptionRequest option1 = new CheckListOptionRequest("선택지1", 1, null);
        CheckListOptionRequest option2 = new CheckListOptionRequest("선택지2", 2, null);
        CheckListItemRequest item1 = new CheckListItemRequest("항목1", 1, null, List.of(option1, option2));
        CheckListItemRequest item2 = new CheckListItemRequest("항목2", 2, null, List.of(option1));

        CheckListCreateRequest request = createRequest(List.of(item1, item2));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class)))
                .thenReturn(mockItem1)
                .thenReturn(mockItem2);
        when(checkListService.saveCheckListOption(any(CheckListOption.class)))
                .thenReturn(mockOption1)
                .thenReturn(mockOption2);

        // when
        CheckListResponse result = createCheckListService.create(projectId, nodeId, userId, request, List.of());

        // then
        assertThat(result.items()).hasSize(2);

        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        verify(checkListAccessValidator).checkProjectDevMemberOrAdmin(projectId);
        verify(checkListService).existNodeCheck(nodeId);
        verify(checkListService).saveCheckList(any(CheckList.class));
        verify(checkListService, times(2)).saveCheckListItem(any(CheckListItem.class));
        verify(checkListService, times(3)).saveCheckListOption(any(CheckListOption.class));
    }

    @Test
    @DisplayName("요청 DTO가 매핑되어 엔티티로 저장되는지 검증한다.")
    void givenRequest_whenCreate_thenEntityMappedSuccessfully() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, null);
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = createRequest(List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);

        // when
        createCheckListService.create(projectId, nodeId, userId, request, List.of());

        // then
        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        verify(checkListAccessValidator).checkProjectDevMemberOrAdmin(projectId);
        verify(checkListService).existNodeCheck(nodeId);
        verify(checkListService).saveCheckList(argThat(checkList ->
                checkList.getCheckListDescription().equals("전달사항") &&
                        checkList.getProjectNodeId().equals(nodeId) &&
                        checkList.getUserId().equals(userId)
        ));
        verify(checkListService).saveCheckListItem(argThat(item ->
                item.getItemTitle().equals("항목1") &&
                        item.getItemOrder().equals(1) &&
                        item.getUserId().equals(userId) &&
                        item.getStatus() == CheckListItemStatus.PENDING
        ));
        verify(checkListService).saveCheckListOption(argThat(option ->
                option.getOptionContent().equals("선택지1") &&
                        option.getOptionOrder().equals(1)
        ));
    }

    @Test
    @DisplayName("템플릿 저장을 요청하면 템플릿 서비스가 호출된다.")
    void givenSaveAsTemplateTrue_whenCreate_thenTemplatesPersisted() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, null);
        CheckListItemRequest itemRequest1 = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListItemRequest itemRequest2 = new CheckListItemRequest("항목2", 2, null, List.of(optionRequest));
        CheckListCreateRequest request = createRequest(List.of(itemRequest1, itemRequest2), true);

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1).thenReturn(mockItem2);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);
        when(createCheckListTemplateService.create(eq(projectId), eq(nodeId), any()))
                .thenReturn(new CheckListTemplateResponse(100L, "템플릿 제목", "템플릿 설명", List.of()));

        // when
        createCheckListService.create(projectId, nodeId, userId, request, List.of());

        // then
        verify(createCheckListTemplateService, times(2))
                .create(eq(projectId), eq(nodeId), any());
    }

    @Test
    @DisplayName("프로젝트 또는 노드 검증 실패 시 체크리스트를 생성할 수 없다.")
    void givenInvalidProjectOrNode_whenCreate_thenThrow() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, null);
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = createRequest(List.of(itemRequest));

        doThrow(new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND))
                .when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);

        // when & then
        assertThatThrownBy(() -> createCheckListService.create(projectId, nodeId, userId, request, List.of()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_NOT_FOUND);

        verify(checkListService, never()).saveCheckList(any(CheckList.class));
    }

    @Test
    @DisplayName("외부 링크는 전체 URL이 fileName에 저장된다.")
    void givenExternalUrl_whenCreate_thenFullUrlStoredInFileName() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        String fileUrl = "https://example.com/path/to/test-file.png";
        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, List.of(fileUrl));
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = createRequest(List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);
        when(checkListService.saveCheckListOptionFile(any(CheckListOptionFile.class))).thenReturn(mockFile1);

        // when
        createCheckListService.create(projectId, nodeId, userId, request, List.of());

        // then
        verify(checkListService).saveCheckListOptionFile(argThat(file ->
                file.getFileUrl().equals(fileUrl) &&
                        file.getFileName().equals(fileUrl) &&  // 외부 링크는 전체 URL 저장
                        file.getFileOrder().equals(0)
        ));
    }

    @Test
    @DisplayName("항목 순서가 중복되면 예외를 발생시킨다.")
    void givenDuplicatedItemOrders_whenCreate_thenThrow() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, null);
        CheckListItemRequest itemRequest1 = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListItemRequest itemRequest2 = new CheckListItemRequest("항목2", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = createRequest(List.of(itemRequest1, itemRequest2));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);

        // when & then
        assertThatThrownBy(() -> createCheckListService.create(projectId, nodeId, userId, request, List.of()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CHECK_LIST_ITEM_ORDER);

        verify(checkListService, never()).saveCheckList(any(CheckList.class));
    }

    @Test
    @DisplayName("옵션 순서가 중복되면 예외를 발생시킨다.")
    void givenDuplicatedOptionOrders_whenCreate_thenThrow() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        CheckListOptionRequest optionRequest1 = new CheckListOptionRequest("선택지1", 1, null);
        CheckListOptionRequest optionRequest2 = new CheckListOptionRequest("선택지2", 1, null);
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest1, optionRequest2));
        CheckListCreateRequest request = createRequest(List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);

        // when & then
        assertThatThrownBy(() -> createCheckListService.create(projectId, nodeId, userId, request, List.of()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CHECK_LIST_OPTION_ORDER);

        verify(checkListService, never()).saveCheckList(any(CheckList.class));
    }

    @Test
    @DisplayName("업로드한 파일 식별자를 fileUrls에 전달하면 업로드 정보를 사용해 저장한다")
    void givenUploadedFiles_whenIdentifierMatches_thenFileSavedFromUpload() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        MultipartFile multipartFile = mock(MultipartFile.class);
        FileUploadResponse uploadResponse = new FileUploadResponse("s3/test-uuid.png", "test.png", "presigned-url");
        when(fileService.uploadFiles(any())).thenReturn(List.of(uploadResponse));

        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, List.of("test.png"));
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = createRequest(List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);
        when(checkListService.saveCheckListOptionFile(any(CheckListOptionFile.class))).thenReturn(mockFile1);

        ArgumentCaptor<CheckListOptionFile> captor = ArgumentCaptor.forClass(CheckListOptionFile.class);

        // when
        createCheckListService.create(projectId, nodeId, userId, request, List.of(multipartFile));

        // then
        verify(checkListService).saveCheckListOptionFile(captor.capture());
        CheckListOptionFile savedFile = captor.getValue();
        assertThat(savedFile.getFileUrl()).isEqualTo("s3/test-uuid.png");
        assertThat(savedFile.getFileName()).isEqualTo("test.png");
    }

    @Test
    @DisplayName("업로드한 파일을 fileUrls에서 참조하지 않으면 예외가 발생한다")
    void givenUploadedFileNotConsumed_whenCreate_thenThrows() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        MultipartFile multipartFile = mock(MultipartFile.class);
        FileUploadResponse uploadResponse = new FileUploadResponse("s3/test-uuid.png", "test.png", "presigned-url");
        when(fileService.uploadFiles(any())).thenReturn(List.of(uploadResponse));

        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, List.of("not-found.png"));
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = createRequest(List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);

        // when & then
        assertThatThrownBy(() -> createCheckListService.create(projectId, nodeId, userId, request, List.of(multipartFile)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHECK_LIST_FILE_MAPPING_NOT_FOUND);
    }

    @Test
    @DisplayName("업로드 파일과 외부 URL을 혼합하여 생성할 수 있다")
    void givenMixedFilesAndUrls_whenCreate_thenSuccess() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        MultipartFile multipartFile = mock(MultipartFile.class);
        FileUploadResponse uploadResponse = new FileUploadResponse("s3/test-uuid.png", "test.png", "presigned-url");
        when(fileService.uploadFiles(any())).thenReturn(List.of(uploadResponse));

        // 업로드 파일 + 외부 URL 혼합
        List<String> fileUrls = Arrays.asList(
                "test.png",  // 업로드된 파일
                "https://example.com/external.pdf",  // 외부 URL
                "https://docs.google.com/document/d/123"  // 외부 URL
        );
        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, fileUrls);
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = createRequest(List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);
        when(checkListService.saveCheckListOptionFile(any(CheckListOptionFile.class))).thenReturn(mockFile1);

        // when
        CheckListResponse result = createCheckListService.create(projectId, nodeId, userId, request, List.of(multipartFile));

        // then
        assertThat(result).isNotNull();
        verify(checkListService, times(3)).saveCheckListOptionFile(any(CheckListOptionFile.class));
    }

    @Test
    @DisplayName("외부 URL만 있는 경우 정상적으로 생성된다")
    void givenOnlyExternalUrls_whenCreate_thenSuccess() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        // 외부 URL만 사용 (업로드 파일 없음)
        List<String> fileUrls = Arrays.asList(
                "https://example.com/file1.pdf",
                "http://example.com/file2.docx",
                "https://www.youtube.com/watch?v=123"
        );
        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, fileUrls);
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = createRequest(List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);
        when(checkListService.saveCheckListOptionFile(any(CheckListOptionFile.class))).thenReturn(mockFile1);

        // when
        CheckListResponse result = createCheckListService.create(projectId, nodeId, userId, request, List.of());

        // then
        assertThat(result).isNotNull();
        verify(checkListService, times(3)).saveCheckListOptionFile(any(CheckListOptionFile.class));
        verify(fileService).uploadFiles(any());  // 빈 리스트로 호출됨
    }

    @Test
    @DisplayName("특수문자가 포함된 URL도 외부 링크로 처리된다")
    void givenUrlWithSpecialCharacters_whenCreate_thenSuccess() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        // 한글, 공백, 특수문자가 포함된 URL (실제로는 인코딩되어 전달되어야 하지만,
        // http/https로 시작하면 외부 링크로 간주)
        List<String> fileUrls = Arrays.asList(
                "https://example.com/파일.pdf",
                "https://example.com/file name.pdf",
                "https://example.com/file|special.pdf"
        );
        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, fileUrls);
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = createRequest(List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);
        when(checkListService.saveCheckListOptionFile(any(CheckListOptionFile.class))).thenReturn(mockFile1);

        // when
        CheckListResponse result = createCheckListService.create(projectId, nodeId, userId, request, List.of());

        // then
        assertThat(result).isNotNull();
        verify(checkListService, times(3)).saveCheckListOptionFile(any(CheckListOptionFile.class));
    }

    private CheckListCreateRequest createRequest(List<CheckListItemRequest> items) {
        return createRequest(items, false, null, null);
    }

    private CheckListCreateRequest createRequest(List<CheckListItemRequest> items, boolean saveAsTemplate) {
        String title = saveAsTemplate ? "템플릿 제목" : null;
        String description = saveAsTemplate ? "템플릿 설명" : null;
        return createRequest(items, saveAsTemplate, title, description);
    }

    private CheckListCreateRequest createRequest(List<CheckListItemRequest> items,
                                                 boolean saveAsTemplate,
                                                 String templateTitle,
                                                 String templateDescription) {
        return new CheckListCreateRequest("전달사항", items, saveAsTemplate, templateTitle, templateDescription);
    }
}
