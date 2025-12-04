package com.workhub.cs.repository.csQna;

import com.workhub.cs.entity.CsQna;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CsQnaRepository extends JpaRepository<CsQna, Long>, CsQnaRepositoryCustom {
}
