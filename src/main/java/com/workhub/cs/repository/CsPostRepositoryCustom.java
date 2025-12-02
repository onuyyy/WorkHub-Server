package com.workhub.cs.repository;

import com.workhub.cs.dto.CsPostSearchRequest;
import com.workhub.cs.entity.CsPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CsPostRepositoryCustom {

    Page<CsPost> findCsPosts(CsPostSearchRequest searchType, Pageable pageable);
}
