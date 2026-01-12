package com.workhub.checklist.service.chekList;

import com.workhub.checklist.dto.checkList.CheckListDetails;
import com.workhub.checklist.dto.checkList.CheckListItemStatus;
import com.workhub.checklist.dto.checkList.CheckListItemUpdateRequest;
import com.workhub.checklist.dto.checkList.CheckListOptionFileUpdateRequest;
import com.workhub.checklist.dto.checkList.CheckListOptionUpdateRequest;
import com.workhub.checklist.dto.checkList.CheckListResponse;
import com.workhub.checklist.dto.checkList.CheckListUpdateCommandType;
import com.workhub.checklist.dto.checkList.CheckListUpdateRequest;
import com.workhub.checklist.dto.checkList.CheckListUserInfo;
import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.checkList.CheckListOption;
import com.workhub.checklist.entity.checkList.CheckListOptionFile;
import com.workhub.checklist.event.CheckListItemStatusChangedEvent;
import com.workhub.checklist.event.CheckListUpdatedEvent;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
import com.workhub.checklist.service.checkList.UpdateCheckListService;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateCheckListServiceTest {

    @Mock
    private CheckListService checkListService;

    @Mock
    private CheckListAccessValidator checkListAccessValidator;

    @Mock
    private FileService fileService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateCheckListService updateCheckListService;

    private MockedStatic<SecurityUtil> securityUtil;
    private CheckList checkList;
    private CheckListItem existingItem;
    private CheckListOption existingOption;
    private CheckListOptionFile existingFile;
    private CheckListUserInfo ownerInfo;

    @BeforeEach
    void setUp() {
        securityUtil = mockStatic(SecurityUtil.class);
        securityUtil.when(SecurityUtil::getCurrentUserIdOrThrow).thenReturn(5L);
        securityUtil.when(SecurityUtil::getCurrentUserRealName).thenReturn(Optional.of("담당자"));
        securityUtil.when(SecurityUtil::getCurrentUserPhone).thenReturn(Optional.of("010-9999-9999"));

        checkList = CheckList.builder()
                .checkListId(1L)
                .checkListDescription("원본 전달사항")
                .projectNodeId(10L)
                .userId(5L)
                .build();

        existingItem = CheckListItem.builder()
                .checkListItemId(11L)
                .checkListId(checkList.getCheckListId())
                .itemTitle("기존 항목")
                .itemOrder(1)
                .templateId(7L)
                .userId(5L)
                .status(CheckListItemStatus.PENDING)
                .build();

        existingOption = CheckListOption.builder()
                .checkListOptionId(101L)
                .checkListItemId(existingItem.getCheckListItemId())
                .optionContent("기존 선택지")
                .optionOrder(1)
                .build();

        existingFile = CheckListOptionFile.builder()
                .checkListOptionFileId(1001L)
                .checkListOptionId(existingOption.getCheckListOptionId())
                .fileUrl("https://cdn.workhub/original.png")
                .fileName("original.png")
                .fileOrder(0)
                .build();

        ownerInfo = CheckListUserInfo.of("담당자", "010-9999-9999");
        lenient().when(fileService.uploadFiles(any())).thenReturn(List.of());
        lenient().doNothing().when(fileService).deleteFiles(any());
    }

    @AfterEach
    void tearDown() {
        securityUtil.close();
    }

    @Test
    @DisplayName("요청에 새 항목이 있으면 생성되고 최신 체크리스트가 반환된다")
    void givenCreateRequest_whenUpdate_thenItemAndOptionAreCreated() {
        // given
        CheckListOptionFileUpdateRequest fileCreate =
                new CheckListOptionFileUpdateRequest(CheckListUpdateCommandType.CREATE, null, "https://cdn.workhub/file.png", 1);
        CheckListOptionUpdateRequest optionCreate =
                new CheckListOptionUpdateRequest(CheckListUpdateCommandType.CREATE, null, "옵션1", 1, List.of(fileCreate));
        CheckListItemUpdateRequest itemCreate =
                new CheckListItemUpdateRequest(CheckListUpdateCommandType.CREATE, null, "새 항목", 1, null, List.of(optionCreate));
        CheckListUpdateRequest request = new CheckListUpdateRequest("최신 전달사항", List.of(itemCreate));

        CheckListDetails details = new CheckListDetails(checkList, List.of(existingItem), List.of(existingOption), List.of(existingFile));
        CheckListResponse expectedResponse = new CheckListResponse(
                checkList.getCheckListId(),
                "최신 전달사항",
                checkList.getProjectNodeId(),
                checkList.getUserId(),
                ownerInfo.userName(),
                ownerInfo.userPhone(),
                List.of()
        );

        given(checkListService.findByNodeId(10L)).willReturn(checkList);
        given(checkListService.saveCheckListItem(any(CheckListItem.class))).willReturn(existingItem);
        given(checkListService.saveCheckListOption(any(CheckListOption.class))).willReturn(existingOption);
        given(checkListService.saveCheckListOptionFile(any(CheckListOptionFile.class))).willReturn(existingFile);
        given(checkListService.findCheckListDetailsById(checkList.getCheckListId())).willReturn(details);
        given(checkListService.resolveUserInfo(checkList.getUserId())).willReturn(ownerInfo);
        given(checkListService.buildResponse(details, ownerInfo)).willReturn(expectedResponse);

        // when
        CheckListResponse response = updateCheckListService.update(1L, 10L, request, List.of());

        // then
        assertThat(response.checkListId()).isEqualTo(checkList.getCheckListId());
        assertThat(response.description()).isEqualTo("최신 전달사항");
        assertThat(checkList.getCheckListDescription()).isEqualTo("최신 전달사항");

        verify(checkListService).saveCheckListItem(any(CheckListItem.class));
        verify(checkListService).saveCheckListOption(any(CheckListOption.class));
        verify(checkListService).saveCheckListOptionFile(any(CheckListOptionFile.class));
        verify(checkListService).findCheckListDetailsById(checkList.getCheckListId());
    }

    @Test
    @DisplayName("항목/선택지/파일 업데이트와 삭제 요청을 처리한다")
    void givenUpdateAndDeleteRequests_whenUpdate_thenEntitiesMutated() {
        // given
        CheckListItem itemToDelete = CheckListItem.builder()
                .checkListItemId(22L)
                .checkListId(checkList.getCheckListId())
                .itemTitle("삭제 대상")
                .itemOrder(3)
                .status(CheckListItemStatus.PENDING)
                .userId(5L)
                .build();

        CheckListOption optionToDelete = CheckListOption.builder()
                .checkListOptionId(202L)
                .checkListItemId(existingItem.getCheckListItemId())
                .optionContent("삭제 선택지")
                .optionOrder(2)
                .build();

        CheckListOption optionUnderDeletedItem = CheckListOption.builder()
                .checkListOptionId(301L)
                .checkListItemId(itemToDelete.getCheckListItemId())
                .optionContent("삭제 항목의 옵션")
                .optionOrder(1)
                .build();

        CheckListOptionFile optionDeleteFile = CheckListOptionFile.builder()
                .checkListOptionFileId(5001L)
                .checkListOptionId(optionToDelete.getCheckListOptionId())
                .fileUrl("https://cdn.workhub/delete.png")
                .fileName("delete.png")
                .fileOrder(0)
                .build();

        CheckListOptionFile fileUnderDeletedItem = CheckListOptionFile.builder()
                .checkListOptionFileId(6001L)
                .checkListOptionId(optionUnderDeletedItem.getCheckListOptionId())
                .fileUrl("https://cdn.workhub/remove.png")
                .fileName("remove.png")
                .fileOrder(0)
                .build();

        CheckListOptionFile fileToDeleteDirect = CheckListOptionFile.builder()
                .checkListOptionFileId(7001L)
                .checkListOptionId(existingOption.getCheckListOptionId())
                .fileUrl("https://cdn.workhub/direct.png")
                .fileName("direct.png")
                .fileOrder(0)
                .build();

        CheckListOptionFileUpdateRequest fileUpdate =
                new CheckListOptionFileUpdateRequest(CheckListUpdateCommandType.UPDATE, existingFile.getCheckListOptionFileId(), "https://cdn.workhub/updated.png", 5);
        CheckListOptionFileUpdateRequest fileDelete =
                new CheckListOptionFileUpdateRequest(CheckListUpdateCommandType.DELETE, fileToDeleteDirect.getCheckListOptionFileId(), null, null);

        CheckListOptionUpdateRequest optionUpdate =
                new CheckListOptionUpdateRequest(CheckListUpdateCommandType.UPDATE, existingOption.getCheckListOptionId(), "수정 선택지", 3, List.of(fileUpdate, fileDelete));
        CheckListOptionUpdateRequest optionDeleteRequest =
                new CheckListOptionUpdateRequest(CheckListUpdateCommandType.DELETE, optionToDelete.getCheckListOptionId(), null, null, null);

        CheckListItemUpdateRequest updateItemRequest = new CheckListItemUpdateRequest(
                CheckListUpdateCommandType.UPDATE,
                existingItem.getCheckListItemId(),
                "수정된 항목",
                2,
                123L,
                List.of(optionUpdate, optionDeleteRequest)
        );

        CheckListItemUpdateRequest deleteItemRequest = new CheckListItemUpdateRequest(
                CheckListUpdateCommandType.DELETE,
                itemToDelete.getCheckListItemId(),
                null,
                null,
                null,
                null
        );

        CheckListUpdateRequest request = new CheckListUpdateRequest(null, List.of(updateItemRequest, deleteItemRequest));

        given(checkListService.findByNodeId(10L)).willReturn(checkList);
        given(checkListService.findCheckListItem(existingItem.getCheckListItemId())).willReturn(existingItem);
        given(checkListService.findCheckListItem(itemToDelete.getCheckListItemId())).willReturn(itemToDelete);
        given(checkListService.findCheckListOption(existingOption.getCheckListOptionId())).willReturn(existingOption);
        given(checkListService.findCheckListOption(optionToDelete.getCheckListOptionId())).willReturn(optionToDelete);
        given(checkListService.findCheckListOptionFile(existingFile.getCheckListOptionFileId())).willReturn(existingFile);
        given(checkListService.findCheckListOptionFile(fileToDeleteDirect.getCheckListOptionFileId())).willReturn(fileToDeleteDirect);
        List<CheckListOption> optionsForDeleteItem = List.of(optionUnderDeletedItem);
        List<CheckListOptionFile> filesForOptionDelete = List.of(optionDeleteFile);
        List<CheckListOptionFile> filesForItemDelete = List.of(fileUnderDeletedItem);

        given(checkListService.findOptionsByCheckListItemId(itemToDelete.getCheckListItemId()))
                .willReturn(optionsForDeleteItem);
        given(checkListService.findOptionFilesByCheckListOptionId(optionToDelete.getCheckListOptionId()))
                .willReturn(filesForOptionDelete);
        given(checkListService.findOptionFilesByCheckListOptionId(optionUnderDeletedItem.getCheckListOptionId()))
                .willReturn(filesForItemDelete);

        CheckListDetails details = new CheckListDetails(checkList, List.of(existingItem), List.of(existingOption), List.of(existingFile));
        CheckListResponse expectedResponse = new CheckListResponse(
                checkList.getCheckListId(),
                checkList.getCheckListDescription(),
                checkList.getProjectNodeId(),
                checkList.getUserId(),
                ownerInfo.userName(),
                ownerInfo.userPhone(),
                List.of()
        );

        given(checkListService.findCheckListDetailsById(checkList.getCheckListId())).willReturn(details);
        given(checkListService.resolveUserInfo(checkList.getUserId())).willReturn(ownerInfo);
        given(checkListService.buildResponse(details, ownerInfo)).willReturn(expectedResponse);

        // when
        CheckListResponse response = updateCheckListService.update(1L, 10L, request, List.of());

        // then
        assertThat(response).isNotNull();
        assertThat(existingItem.getItemTitle()).isEqualTo("수정된 항목");
        assertThat(existingOption.getOptionContent()).isEqualTo("수정 선택지");
        assertThat(existingFile.getFileUrl()).isEqualTo("https://cdn.workhub/updated.png");

        verify(checkListService, times(2)).findCheckListItem(anyLong());
        verify(checkListService).findCheckListOption(existingOption.getCheckListOptionId());
        verify(checkListService).findCheckListOption(optionToDelete.getCheckListOptionId());
        verify(checkListService).findCheckListOptionFile(existingFile.getCheckListOptionFileId());
        verify(checkListService).findCheckListOptionFile(fileToDeleteDirect.getCheckListOptionFileId());
        verify(checkListService).deleteCheckListOption(optionToDelete);
        verify(checkListService).deleteCheckListOptionFile(fileToDeleteDirect);
        verify(checkListService).deleteCheckListOptionFiles(filesForOptionDelete);
        verify(checkListService).deleteCheckListOptionFiles(filesForItemDelete);
        verify(checkListService).deleteCheckListOptions(optionsForDeleteItem);
        verify(checkListService).deleteCheckListItem(itemToDelete);
        verify(eventPublisher).publishEvent(any(CheckListUpdatedEvent.class));
    }

    @Test
    @DisplayName("클라이언트가 항목 상태를 수정하면 히스토리가 남고 상태가 변경된다")
    void givenValidClientRequest_whenUpdateStatus_thenUpdatesItemAndRecordsHistory() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long checkListId = checkList.getCheckListId();
        Long itemId = 777L;

        CheckListItem item = CheckListItem.builder()
                .checkListItemId(itemId)
                .checkListId(checkListId)
                .status(CheckListItemStatus.PENDING)
                .build();

        CheckListOption selectedOption = CheckListOption.builder()
                .checkListOptionId(1000L)
                .checkListItemId(itemId)
                .optionContent("선택된 옵션")
                .optionOrder(1)
                .isSelected(true)
                .build();

        given(checkListService.findById(checkListId)).willReturn(checkList);
        given(checkListService.findCheckListItem(itemId)).willReturn(item);
        given(checkListService.findOptionsByCheckListItemId(itemId)).willReturn(List.of(selectedOption));

        // when
        CheckListItemStatus result = updateCheckListService.updateStatus(
                projectId,
                nodeId,
                checkListId,
                itemId,
                CheckListItemStatus.AGREED
        );

        // then
        assertThat(result).isEqualTo(CheckListItemStatus.AGREED);
        assertThat(item.getStatus()).isEqualTo(CheckListItemStatus.AGREED);

        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        verify(checkListAccessValidator).chekProjectClientMember(projectId);
        verify(checkListService).snapShotAndRecordHistory(item, itemId, ActionType.UPDATE);
        verify(eventPublisher).publishEvent(any(CheckListItemStatusChangedEvent.class));
    }

    @Test
    @DisplayName("동의 처리 시 선택된 옵션이 없으면 예외가 발생한다")
    void givenNoSelectedOption_whenUpdateStatusToAgreed_thenThrowsBusinessException() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long checkListId = checkList.getCheckListId();
        Long itemId = 779L;

        CheckListItem item = CheckListItem.builder()
                .checkListItemId(itemId)
                .checkListId(checkListId)
                .status(CheckListItemStatus.PENDING)
                .build();

        CheckListOption unselectedOption = CheckListOption.builder()
                .checkListOptionId(2000L)
                .checkListItemId(itemId)
                .optionContent("미선택 옵션")
                .optionOrder(1)
                .isSelected(false)
                .build();

        given(checkListService.findById(checkListId)).willReturn(checkList);
        given(checkListService.findCheckListItem(itemId)).willReturn(item);
        given(checkListService.findOptionsByCheckListItemId(itemId)).willReturn(List.of(unselectedOption));

        // when & then
        assertThatThrownBy(() -> updateCheckListService.updateStatus(
                projectId,
                nodeId,
                checkListId,
                itemId,
                CheckListItemStatus.AGREED
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.CHECK_LIST_OPTION_NOT_SELECTED.getMessage());

        verify(checkListService, never()).snapShotAndRecordHistory(item, itemId, ActionType.UPDATE);
    }

    @Test
    @DisplayName("요청한 체크리스트에 속하지 않은 항목이면 예외가 발생한다")
    void givenMismatchedItem_whenUpdateStatus_thenThrowsBusinessException() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long checkListId = checkList.getCheckListId();
        Long itemId = 888L;

        CheckListItem item = CheckListItem.builder()
                .checkListItemId(itemId)
                .checkListId(999L)
                .status(CheckListItemStatus.PENDING)
                .build();

        given(checkListService.findById(checkListId)).willReturn(checkList);
        given(checkListService.findCheckListItem(itemId)).willReturn(item);

        // when & then
        assertThatThrownBy(() -> updateCheckListService.updateStatus(
                projectId,
                nodeId,
                checkListId,
                itemId,
                CheckListItemStatus.ON_HOLD
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.CHECK_LIST_ITEM_NOT_BELONG_TO_CHECK_LIST.getMessage());

        verify(checkListService, never()).snapShotAndRecordHistory(item, itemId, ActionType.UPDATE);
    }

    @Test
    @DisplayName("status 값이 없으면 기존 상태를 유지하지만 히스토리는 남는다")
    void givenNullStatus_whenUpdateStatus_thenKeepsCurrentStatusAndRecordsHistory() {
        // given
        Long projectId = 1L;
        Long nodeId = 10L;
        Long checkListId = checkList.getCheckListId();
        Long itemId = 889L;

        CheckListItem item = CheckListItem.builder()
                .checkListItemId(itemId)
                .checkListId(checkListId)
                .status(CheckListItemStatus.PENDING)
                .build();

        given(checkListService.findById(checkListId)).willReturn(checkList);
        given(checkListService.findCheckListItem(itemId)).willReturn(item);

        // when
        CheckListItemStatus result = updateCheckListService.updateStatus(
                projectId,
                nodeId,
                checkListId,
                itemId,
                null
        );

        // then
        assertThat(result).isEqualTo(CheckListItemStatus.PENDING);
        assertThat(item.getStatus()).isEqualTo(CheckListItemStatus.PENDING);

        verify(checkListService).snapShotAndRecordHistory(item, itemId, ActionType.UPDATE);
    }
}
