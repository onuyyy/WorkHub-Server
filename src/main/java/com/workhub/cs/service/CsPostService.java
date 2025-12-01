package com.workhub.cs.service;

import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import com.workhub.cs.repository.CsPostFileRepository;
import com.workhub.cs.repository.CsPostRepository;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CsPostService {

    private final CsPostRepository csPostRepository;
    private final CsPostFileRepository csPostFileRepository;

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
}
