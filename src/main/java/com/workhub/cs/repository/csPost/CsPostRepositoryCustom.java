package com.workhub.cs.repository.csPost;

import com.workhub.cs.dto.csPost.CsPostSearchRequest;
import com.workhub.cs.entity.CsPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CsPostRepositoryCustom {

    Page<CsPost> findCsPosts(CsPostSearchRequest searchType, Pageable pageable);
}
