package com.workhub.cs.repository.csPost;

import com.workhub.cs.entity.CsPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CsPostRepository extends JpaRepository<CsPost,Long>, CsPostRepositoryCustom {
}
