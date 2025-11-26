package com.workhub.cs.service;

import com.workhub.cs.dto.CsPostFileRequest;
import com.workhub.cs.dto.CsPostRequest;
import com.workhub.cs.dto.CsPostResponse;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import com.workhub.cs.repository.CsPostFileRepository;
import com.workhub.cs.repository.CsPostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CsPostService {

    private final CsPostRepository csPostRepository;
    private final CsPostFileRepository csPostFileRepository;

//    private final ProjectValidator projectValidator;
//    private final UserValidator userValidator;

    /**
     * CS 게시글을 작성합니다.
     * @param projectId
     * @param request
     * @return
     */
    public CsPostResponse create(Long projectId, CsPostRequest request) {

        // todo : userId, projectId 검증 validator 로직 필요
        // todo : project가 끝난 상태인지, 존재하는 프로젝트인지 확인 필요
        CsPost post = csPostRepository.save(CsPost.of(projectId, request));

        List<CsPostFile> files = List.of();

        if (request.files() != null && !request.files().isEmpty()) {

            files = request.files().stream()
                    .map(f -> CsPostFile.of(post.getCsPostId(), f))
                    .toList();

            csPostFileRepository.saveAll(files);
        }

        return CsPostResponse.from(post, files);
    }
}
