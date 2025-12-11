package com.workhub.checklist.service;

import com.workhub.checklist.dto.CheckListDetails;
import com.workhub.checklist.dto.CheckListItemUpdateRequest;
import com.workhub.checklist.dto.CheckListOptionFileUpdateRequest;
import com.workhub.checklist.dto.CheckListOptionUpdateRequest;
import com.workhub.checklist.dto.CheckListResponse;
import com.workhub.checklist.dto.CheckListUpdateCommandType;
import com.workhub.checklist.dto.CheckListUpdateRequest;
import com.workhub.checklist.entity.CheckList;
import com.workhub.checklist.entity.CheckListItem;
import com.workhub.checklist.entity.CheckListOption;
import com.workhub.checklist.entity.CheckListOptionFile;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateCheckListServiceTest {

    @Mock
    private CheckListService checkListService;

    @Mock
    private CheckListAccessValidator checkListAccessValidator;

    @InjectMocks
    private UpdateCheckListService updateCheckListService;

    private MockedStatic<SecurityUtil> securityUtil;
    private CheckList checkList;
    private CheckListItem existingItem;
    private CheckListOption existingOption;
    private CheckListOptionFile existingFile;

    @BeforeEach
    void setUp() {
        securityUtil = mockStatic(SecurityUtil.class);
        securityUtil.when(SecurityUtil::getCurrentUserIdOrThrow).thenReturn(99L);

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
                .confirm(false)
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

        given(checkListService.findByNodeId(10L)).willReturn(checkList);
        given(checkListService.saveCheckListItem(any(CheckListItem.class))).willReturn(existingItem);
        given(checkListService.saveCheckListOption(any(CheckListOption.class))).willReturn(existingOption);
        given(checkListService.saveCheckListOptionFile(any(CheckListOptionFile.class))).willReturn(existingFile);
        given(checkListService.findCheckListDetailsById(checkList.getCheckListId()))
                .willReturn(new CheckListDetails(checkList, List.of(existingItem), List.of(existingOption), List.of(existingFile)));

        // when
        CheckListResponse response = updateCheckListService.update(1L, 10L, request);

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
                .confirm(false)
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
        given(checkListService.findCheckListDetailsById(checkList.getCheckListId()))
                .willReturn(new CheckListDetails(checkList, List.of(existingItem), List.of(existingOption), List.of(existingFile)));

        // when
        CheckListResponse response = updateCheckListService.update(1L, 10L, request);

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
    }
}
