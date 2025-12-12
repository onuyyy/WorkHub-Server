package com.workhub.checklist.service.comment;

import com.workhub.checklist.entity.CheckListItemComment;
import com.workhub.checklist.repository.CheckListItemCommentRepository;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckListCommentService {

    private final CheckListItemCommentRepository checkListCommentRepository;

    public CheckListItemComment save(CheckListItemComment checkListItemComment) {
        return checkListCommentRepository.save(checkListItemComment);
    }

    public CheckListItemComment findById(Long id) {
        return checkListCommentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_CHECK_LIST_ITEM_COMMENT));
    }

}
