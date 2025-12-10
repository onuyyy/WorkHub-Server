package com.workhub.projectNode.service;

import com.workhub.global.util.SecurityUtil;
import com.workhub.projectNode.dto.NodeListResponse;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReadProjectNodeService {

    private final ProjectNodeService projectNodeService;
    private final ProjectNodeValidator projectNodeValidator;
    private final UserService userService;

    /**
     * 프로젝트의 모든 노드 리스트를 조회
     * 노드는 순서(nodeOrder)를 기준으로 정렬되어 반환됩니다.
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트에 속한 노드 리스트 (nodeOrder 기준 정렬)
     */
    public List<NodeListResponse> getNodeListByProject(Long projectId) {

        Long loginUser = SecurityUtil.getCurrentUserIdOrThrow();
        projectNodeValidator.validateProjectMemberPermission(projectId, loginUser);

        List<ProjectNode> nodeList = projectNodeService.findByProjectIdByNodeOrder(projectId);
        List<Long> devMembers = nodeList.stream()
                .map(ProjectNode::getDeveloperUserId)
                .toList();

        Map<Long, UserTable> userMap = userService.getUserMapByUserIdIn(devMembers);

        return nodeList.stream()
                .map(node -> NodeListResponse.from(node, userMap.get(node.getDeveloperUserId())))
                .toList();
    }
}
