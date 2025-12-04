package com.workhub.cs.repository.csQna;

import com.workhub.cs.entity.CsQna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CsQnaRepository extends JpaRepository<CsQna, Long>, CsQnaRepositoryCustom {

    /**
     * 특정 부모 댓글의 모든 자식 댓글을 조회한다.
     */
    List<CsQna> findByParentQnaId(Long parentQnaId);
}
