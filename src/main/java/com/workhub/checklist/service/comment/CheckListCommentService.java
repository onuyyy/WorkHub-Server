package com.workhub.checklist.service.comment;

import com.workhub.checklist.entity.comment.CheckListItemComment;
import com.workhub.checklist.entity.comment.CheckListItemCommentFile;
import com.workhub.checklist.repository.CheckListItemCommentFileRepository;
import com.workhub.checklist.repository.CheckListItemCommentRepository;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckListCommentService {

    private final CheckListItemCommentRepository checkListCommentRepository;
    private final CheckListItemCommentFileRepository checkListItemCommentFileRepository;

    public CheckListItemComment save(CheckListItemComment checkListItemComment) {
        return checkListCommentRepository.save(checkListItemComment);
    }

    public CheckListItemComment findById(Long id) {
        return checkListCommentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_CHECK_LIST_ITEM_COMMENT));
    }

    public CheckListItemCommentFile saveCommentFile(CheckListItemCommentFile commentFile) {
        return checkListItemCommentFileRepository.save(commentFile);
    }

    public List<CheckListItemCommentFile> findCommentFilesByCommentId(Long commentId) {
        return checkListItemCommentFileRepository.findAllByClCommentIdOrderByFileOrderAsc(commentId);
    }

    public void deleteCommentFiles(List<CheckListItemCommentFile> files) {
        checkListItemCommentFileRepository.deleteAll(files);
    }

    public List<CheckListItemComment> findAllByCheckListItemId(Long checkListItemId) {
        return checkListCommentRepository.findAllByCheckListItemId(checkListItemId);
    }

    public List<CheckListItemComment> findTopLevelCommentsByCheckListItemId(Long checkListItemId) {
        return checkListCommentRepository.findTopLevelCommentsByCheckListItemId(checkListItemId);
    }

}
