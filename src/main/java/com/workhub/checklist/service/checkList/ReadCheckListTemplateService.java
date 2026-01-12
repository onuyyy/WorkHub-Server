package com.workhub.checklist.service.checkList;

import com.workhub.checklist.dto.checkList.CheckListItemResponse;
import com.workhub.checklist.dto.checkList.CheckListOptionFileResponse;
import com.workhub.checklist.dto.checkList.CheckListOptionResponse;
import com.workhub.checklist.dto.checkList.CheckListTemplateResponse;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.checkList.CheckListOption;
import com.workhub.checklist.entity.checkList.CheckListOptionFile;
import com.workhub.checklist.entity.checkList.CheckListTemplate;
import com.workhub.checklist.repository.CheckListItemRepository;
import com.workhub.checklist.repository.CheckListOptionFileRepository;
import com.workhub.checklist.repository.CheckListOptionRepository;
import com.workhub.checklist.service.CheckListAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadCheckListTemplateService {

    private final CheckListTemplateService checkListTemplateService;
    private final CheckListAccessValidator checkListAccessValidator;
    private final CheckListItemRepository checkListItemRepository;
    private final CheckListOptionRepository checkListOptionRepository;
    private final CheckListOptionFileRepository checkListOptionFileRepository;

    /**
     * 체크리스트 템플릿 리스트를 가져온다.
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @return 템플릿 리스트
     */
    public List<CheckListTemplateResponse> findAll(Long projectId, Long nodeId) {
        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.checkProjectMemberOrAdmin(projectId);

        return checkListTemplateService.findAll().stream()
                .map(CheckListTemplateResponse::from)
                .toList();
    }

    /**
     * 템플릿 ID로 단건 조회한다. 클라이언트에서 작성 폼에 불러올 때 사용한다.
     * 템플릿과 연결된 모든 항목(items), 선택지(options), 파일(files)을 계층 구조로 반환한다.
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @param templateId 템플릿 식별자
     * @return CheckListTemplateResponse (전체 계층 구조 포함)
     */
    public CheckListTemplateResponse findById(Long projectId, Long nodeId, Long templateId) {
        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.checkProjectMemberOrAdmin(projectId);

        // 1) 템플릿 기본 정보 조회
        CheckListTemplate template = checkListTemplateService.findById(templateId);

        // 2) 해당 템플릿으로 생성된 모든 CheckListItem 조회
        List<CheckListItem> items = checkListItemRepository.findAllByTemplateIdOrderByItemOrderAsc(templateId);

        // 3) 모든 Item의 ID 추출
        List<Long> itemIds = items.stream()
                .map(CheckListItem::getCheckListItemId)
                .toList();

        // 4) 모든 Option 조회 후 itemId로 그룹핑
        List<CheckListOption> allOptions = itemIds.stream()
                .flatMap(itemId -> checkListOptionRepository.findAllByCheckListItemIdOrderByOptionOrderAsc(itemId).stream())
                .toList();

        Map<Long, List<CheckListOption>> optionsByItemId = allOptions.stream()
                .collect(Collectors.groupingBy(CheckListOption::getCheckListItemId));

        // 5) 모든 File 조회 후 optionId로 그룹핑
        List<Long> optionIds = allOptions.stream()
                .map(CheckListOption::getCheckListOptionId)
                .toList();

        List<CheckListOptionFile> allFiles = optionIds.stream()
                .flatMap(optionId -> checkListOptionFileRepository.findAllByCheckListOptionIdOrderByFileOrderAsc(optionId).stream())
                .toList();

        Map<Long, List<CheckListOptionFile>> filesByOptionId = allFiles.stream()
                .collect(Collectors.groupingBy(CheckListOptionFile::getCheckListOptionId));

        // 6) 계층 구조로 변환
        List<CheckListItemResponse> itemResponses = items.stream()
                .map(item -> buildItemResponse(item, optionsByItemId, filesByOptionId))
                .toList();

        return CheckListTemplateResponse.withItems(template, itemResponses);
    }

    /**
     * CheckListItem을 CheckListItemResponse로 변환한다.
     */
    private CheckListItemResponse buildItemResponse(
            CheckListItem item,
            Map<Long, List<CheckListOption>> optionsByItemId,
            Map<Long, List<CheckListOptionFile>> filesByOptionId
    ) {
        List<CheckListOptionResponse> optionResponses =
                optionsByItemId.getOrDefault(item.getCheckListItemId(), List.of()).stream()
                        .map(option -> buildOptionResponse(option, filesByOptionId))
                        .toList();

        return CheckListItemResponse.from(item, optionResponses);
    }

    /**
     * CheckListOption을 CheckListOptionResponse로 변환한다.
     */
    private CheckListOptionResponse buildOptionResponse(
            CheckListOption option,
            Map<Long, List<CheckListOptionFile>> filesByOptionId
    ) {
        List<CheckListOptionFileResponse> fileResponses =
                filesByOptionId.getOrDefault(option.getCheckListOptionId(), List.of()).stream()
                        .map(CheckListOptionFileResponse::from)
                        .toList();

        return CheckListOptionResponse.from(option, fileResponses);
    }
}
