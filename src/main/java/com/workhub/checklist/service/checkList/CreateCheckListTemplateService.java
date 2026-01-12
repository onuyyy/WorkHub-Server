package com.workhub.checklist.service.checkList;

import com.workhub.checklist.dto.checkList.CheckListTemplateRequest;
import com.workhub.checklist.dto.checkList.CheckListTemplateResponse;
import com.workhub.checklist.entity.checkList.CheckListTemplate;
import com.workhub.checklist.service.CheckListAccessValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateCheckListTemplateService {

    private final CheckListTemplateService checkListTemplateService;
    private final CheckListAccessValidator checkListAccessValidator;

    /**
     * 체크리스트 템플릿을 등록한다.
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @param request 요청 정보
     * @return CheckListTemplateResponse
     */
    public CheckListTemplateResponse create(Long projectId,
                                            Long nodeId,
                                            CheckListTemplateRequest request) {

        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.checkProjectDevMemberOrAdmin(projectId);

        CheckListTemplate template = CheckListTemplate.of(
                request.itemTitle().trim(),
                normalizeDescription(request.description())
        );

        CheckListTemplate saved = checkListTemplateService.save(template);

        return CheckListTemplateResponse.from(saved);
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
