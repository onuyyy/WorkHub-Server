package com.workhub.checklist.service.checkList;

import com.workhub.checklist.dto.checkList.CheckListDetails;
import com.workhub.checklist.dto.checkList.CheckListResponse;
import com.workhub.checklist.dto.checkList.CheckListUserInfo;
import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.userTable.dto.user.response.UserDetailResponse;
import com.workhub.userTable.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadCheckListService {

    private final CheckListService checkListService;
    private final CheckListAccessValidator checkListAccessValidator;
    private final UserService userService;
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

        UserDetailResponse user = userService.getUser(checkList.getUserId());

        return checkListService.buildResponse(
                details,
                CheckListUserInfo.of(user)
        );
    }
}
