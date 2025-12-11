package com.workhub.checklist.service;

import com.workhub.checklist.dto.*;
import com.workhub.checklist.entity.CheckList;
import com.workhub.checklist.entity.CheckListItem;
import com.workhub.checklist.entity.CheckListOption;
import com.workhub.checklist.entity.CheckListOptionFile;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private CreateCheckListService createCheckListService;

    private CheckList mockCheckList;
    private CheckListItem mockItem1;
    private CheckListItem mockItem2;
    private CheckListOption mockOption1;
    private CheckListOption mockOption2;
    private CheckListOptionFile mockFile1;

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
        CheckListCreateRequest request = new CheckListCreateRequest("전달사항", List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);

        // when
        CheckListResponse result = createCheckListService.create(projectId, nodeId, userId, request);

        // then
        assertThat(result.checkListId()).isEqualTo(1L);
        assertThat(result.description()).isEqualTo("전달사항");
        assertThat(result.projectNodeId()).isEqualTo(10L);
        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.items()).hasSize(1);

        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        verify(checkListService).saveCheckList(any(CheckList.class));
        verify(checkListService).saveCheckListItem(any(CheckListItem.class));
        verify(checkListService).saveCheckListOption(any(CheckListOption.class));
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
        CheckListCreateRequest request = new CheckListCreateRequest("전달사항", List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);
        when(checkListService.saveCheckListOptionFile(any(CheckListOptionFile.class))).thenReturn(mockFile1);

        // when
        createCheckListService.create(projectId, nodeId, userId, request);

        // then
        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
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

        CheckListCreateRequest request = new CheckListCreateRequest("전달사항", List.of(item1, item2));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class)))
                .thenReturn(mockItem1)
                .thenReturn(mockItem2);
        when(checkListService.saveCheckListOption(any(CheckListOption.class)))
                .thenReturn(mockOption1)
                .thenReturn(mockOption2);

        // when
        CheckListResponse result = createCheckListService.create(projectId, nodeId, userId, request);

        // then
        assertThat(result.items()).hasSize(2);

        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
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
        CheckListCreateRequest request = new CheckListCreateRequest("전달사항", List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);

        // when
        createCheckListService.create(projectId, nodeId, userId, request);

        // then
        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
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
    @DisplayName("프로젝트 또는 노드 검증 실패 시 체크리스트를 생성할 수 없다.")
    void givenInvalidProjectOrNode_whenCreate_thenThrow() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, null);
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = new CheckListCreateRequest("전달사항", List.of(itemRequest));

        doThrow(new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND))
                .when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);

        // when & then
        assertThatThrownBy(() -> createCheckListService.create(projectId, nodeId, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_NOT_FOUND);

        verify(checkListService, never()).saveCheckList(any(CheckList.class));
    }

    @Test
    @DisplayName("파일 URL에서 파일명이 올바르게 추출되어 저장되는지 검증한다.")
    void givenFileUrl_whenCreate_thenFileNameExtracted() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long userId = 2L;

        String fileUrl = "https://example.com/path/to/test-file.png";
        CheckListOptionRequest optionRequest = new CheckListOptionRequest("선택지1", 1, List.of(fileUrl));
        CheckListItemRequest itemRequest = new CheckListItemRequest("항목1", 1, null, List.of(optionRequest));
        CheckListCreateRequest request = new CheckListCreateRequest("전달사항", List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        when(checkListService.saveCheckList(any(CheckList.class))).thenReturn(mockCheckList);
        when(checkListService.saveCheckListItem(any(CheckListItem.class))).thenReturn(mockItem1);
        when(checkListService.saveCheckListOption(any(CheckListOption.class))).thenReturn(mockOption1);
        when(checkListService.saveCheckListOptionFile(any(CheckListOptionFile.class))).thenReturn(mockFile1);

        // when
        createCheckListService.create(projectId, nodeId, userId, request);

        // then
        verify(checkListService).saveCheckListOptionFile(argThat(file ->
                file.getFileUrl().equals(fileUrl) &&
                        file.getFileName().equals("test-file.png") &&
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
        CheckListCreateRequest request = new CheckListCreateRequest("전달사항", List.of(itemRequest1, itemRequest2));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);

        // when & then
        assertThatThrownBy(() -> createCheckListService.create(projectId, nodeId, userId, request))
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
        CheckListCreateRequest request = new CheckListCreateRequest("전달사항", List.of(itemRequest));

        doNothing().when(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);

        // when & then
        assertThatThrownBy(() -> createCheckListService.create(projectId, nodeId, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CHECK_LIST_OPTION_ORDER);

        verify(checkListService, never()).saveCheckList(any(CheckList.class));
    }
}
