package com.workhub.checklist.service;

import com.workhub.checklist.dto.CheckListDetails;
import com.workhub.checklist.dto.CheckListItemStatus;
import com.workhub.checklist.dto.CheckListResponse;
import com.workhub.checklist.entity.CheckList;
import com.workhub.checklist.entity.CheckListItem;
import com.workhub.checklist.entity.CheckListOption;
import com.workhub.checklist.entity.CheckListOptionFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadCheckListServiceTest {

    @Mock
    private CheckListService checkListService;

    @Mock
    private CheckListAccessValidator checkListAccessValidator;

    @InjectMocks
    private ReadCheckListService readCheckListService;

    @Test
    void givenProjectAndNode_whenFindCheckList_thenReturnsResponse() {
        // given
        Long projectId = 1L;
        Long nodeId = 2L;
        Long checkListId = 3L;

        CheckList checkList = CheckList.builder()
                .checkListId(checkListId)
                .checkListDescription("테스트 체크리스트")
                .projectNodeId(nodeId)
                .userId(10L)
                .build();

        CheckListItem item = CheckListItem.builder()
                .checkListItemId(11L)
                .itemTitle("아이템")
                .itemOrder(1)
                .status(CheckListItemStatus.PENDING)
                .confirmedAt(LocalDateTime.now())
                .checkListId(checkListId)
                .templateId(100L)
                .userId(20L)
                .build();

        CheckListOption option = CheckListOption.builder()
                .checkListOptionId(21L)
                .optionContent("옵션")
                .optionOrder(1)
                .checkListItemId(item.getCheckListItemId())
                .build();

        CheckListOptionFile file = CheckListOptionFile.builder()
                .checkListOptionFileId(31L)
                .fileUrl("https://example.com/file.png")
                .fileName("file.png")
                .fileOrder(0)
                .checkListOptionId(option.getCheckListOptionId())
                .build();

        CheckListDetails details = new CheckListDetails(checkList, List.of(item), List.of(option), List.of(file));

        when(checkListService.findByNodeId(nodeId)).thenReturn(checkList);
        when(checkListService.findCheckListDetailsById(checkListId)).thenReturn(details);

        // when
        CheckListResponse response = readCheckListService.findCheckList(projectId, nodeId);

        // then
        assertThat(response.checkListId()).isEqualTo(checkListId);
        assertThat(response.description()).isEqualTo("테스트 체크리스트");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).options()).hasSize(1);
        assertThat(response.items().get(0).options().get(0).files()).hasSize(1);
        assertThat(response.items().get(0).options().get(0).files().get(0).fileUrl()).isEqualTo("https://example.com/file.png");

        verify(checkListAccessValidator).validateProjectAndNode(projectId, nodeId);
        verify(checkListAccessValidator).checkProjectDevMember(projectId);
        verify(checkListAccessValidator).chekProjectClientMember(projectId);
        verify(checkListService).findByNodeId(nodeId);
        verify(checkListService).findCheckListDetailsById(checkListId);
    }
}
