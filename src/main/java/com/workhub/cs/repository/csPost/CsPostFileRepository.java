package com.workhub.cs.repository.csPost;

import com.workhub.cs.entity.CsPostFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CsPostFileRepository extends JpaRepository<CsPostFile, Long> {

    List<CsPostFile> findByCsPostId(Long csPostId);
}