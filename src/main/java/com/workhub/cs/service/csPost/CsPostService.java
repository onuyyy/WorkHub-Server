package com.workhub.cs.service.csPost;

import com.workhub.cs.dto.csPost.CsPostHistorySnapshot;
import com.workhub.cs.dto.csPost.CsPostSearchRequest;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import com.workhub.cs.repository.csPost.CsPostFileRepository;
import com.workhub.cs.repository.csPost.CsPostRepository;
import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CsPostService {

    private final CsPostRepository csPostRepository;
    private final CsPostFileRepository csPostFileRepository;
    private final HistoryRecorder historyRecorder;

    /**
     * CS POST 엔티티를 저장한다.
     */
    public CsPost save(CsPost csPost) {
        return csPostRepository.save(csPost);
    }

    /**
     * 식별자로 CS POST를 조회한다.
     */
    public CsPost findById(Long csPostId) {
        return csPostRepository.findById(csPostId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_CS_POST));
    }

    /**
     * 첨부 파일 목록을 일괄 저장한다.
     */
    public List<CsPostFile> saveAllFiles(List<CsPostFile> files) {
        return csPostFileRepository.saveAll(files);
    }

    /**
     * 단일 첨부 파일을 저장한다.
     */
    public CsPostFile saveFile(CsPostFile file) {
        return csPostFileRepository.save(file);
    }

    /**
     * CS POST 연결 파일을 모두 조회한다.
     */
    public List<CsPostFile> findFilesByCsPostId(Long csPostId) {
        return csPostFileRepository.findByCsPostId(csPostId);
    }

    public Page<CsPost> findCsPosts(CsPostSearchRequest searchType, Pageable pageable) {
        return csPostRepository.findCsPosts(searchType, pageable);
    }

    /**
     * 스냅샷으로 변환 후 히스토리 엔티티에 저장
     * @param csPost
     * @param csPostId
     * @param actionType
     */
    public void snapShotAndRecordHistory(CsPost csPost, Long csPostId, ActionType actionType) {
        CsPostHistorySnapshot snapshot = CsPostHistorySnapshot.from(csPost);
        historyRecorder.recordHistory(HistoryType.CS_POST, csPostId, actionType, snapshot);
    }
}
