package com.workhub.checklist.service;

import com.workhub.checklist.dto.*;
import com.workhub.checklist.entity.CheckList;
import com.workhub.checklist.entity.CheckListItem;
import com.workhub.checklist.entity.CheckListOption;
import com.workhub.checklist.entity.CheckListOptionFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadCheckListService {

    private final CheckListService checkListService;
    private final CheckListAccessValidator checkListAccessValidator;

    /**
     * 프로젝트/노드 권한을 검증한 뒤 체크리스트 전체 계층을 Response로 만들어 반환한다.
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @return CheckListResponse
     */
    public CheckListResponse findCheckList(Long projectId, Long nodeId) {
        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);

        CheckList checkList = checkListService.findByNodeId(nodeId);

        CheckListDetails details = checkListService
                .findCheckListDetailsById(checkList.getCheckListId());

        Map<Long, List<CheckListOptionFile>> filesByOptionId = details.getFiles().stream()
                .collect(Collectors.groupingBy(CheckListOptionFile::getCheckListOptionId));

        Map<Long, List<CheckListOption>> optionsByItemId = details.getOptions().stream()
                .collect(Collectors.groupingBy(CheckListOption::getCheckListItemId));

        List<CheckListItemResponse> itemResponses = details.getItems().stream()
                .map(item -> toItemResponse(item, optionsByItemId, filesByOptionId))
                .collect(Collectors.toList());

        return CheckListResponse.from(details.getCheckList(), itemResponses);
    }

    /**
     * 단일 체크리스트 아이템을 Response로 변환하며 하위 옵션 목록을 함께 채운다.
     * @param item 체크리스트 아이템
     * @param optionsByItemId 옵션 아이디
     * @param filesByOptionId 파일 옵션 아이디
     * @return CheckListItemResponse
     */
    private CheckListItemResponse toItemResponse(
            CheckListItem item,
            Map<Long, List<CheckListOption>> optionsByItemId,
            Map<Long, List<CheckListOptionFile>> filesByOptionId) {

        List<CheckListOptionResponse> optionResponses =
                optionsByItemId.getOrDefault(item.getCheckListItemId(), List.of()).stream()
                        .map(option -> toOptionResponse(option, filesByOptionId))
                        .collect(Collectors.toList());

        return CheckListItemResponse.from(item, optionResponses);
    }

    /**
     * 옵션과 그 하위 파일들을 Response 구조로 변환한다.
     * @param option 체크리스트 옵션
     * @param filesByOptionId 옵션 아이디
     * @return CheckListOptionResponse
     */
    private CheckListOptionResponse toOptionResponse(
            CheckListOption option,
            Map<Long, List<CheckListOptionFile>> filesByOptionId) {

        List<CheckListOptionFileResponse> fileResponses =
                filesByOptionId.getOrDefault(option.getCheckListOptionId(), List.of()).stream()
                        .map(CheckListOptionFileResponse::from)
                        .collect(Collectors.toList());

        return CheckListOptionResponse.from(option, fileResponses);
    }
}
