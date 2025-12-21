package com.workhub.checklist.service.checkList;

import com.workhub.checklist.entity.checkList.CheckListTemplate;
import com.workhub.checklist.repository.CheckListTemplateRepository;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckListTemplateService {

    private final CheckListTemplateRepository checkListTemplateRepository;

    public CheckListTemplate save(CheckListTemplate template) {
        return checkListTemplateRepository.save(template);
    }

    public List<CheckListTemplate> findAll() {
        return checkListTemplateRepository.findAllByOrderByTemplateIdDesc();
    }

    public CheckListTemplate findById(Long templateId) {
        return checkListTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECK_LIST_TEMPLATE_NOT_FOUND));
    }
}
