package com.workhub.checklist.service;

import com.workhub.checklist.dto.CheckListDetails;
import com.workhub.checklist.dto.CheckListResponse;
import com.workhub.checklist.entity.CheckList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        checkListAccessValidator.checkProjectMemberOrAdmin(projectId);

        CheckList checkList = checkListService.findByNodeId(nodeId);
        CheckListDetails details = checkListService.findCheckListDetailsById(checkList.getCheckListId());

        return checkListService.buildResponse(details);
    }
}
