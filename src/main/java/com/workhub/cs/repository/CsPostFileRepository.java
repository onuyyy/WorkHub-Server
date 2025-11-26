package com.workhub.cs.repository;

import com.workhub.cs.entity.CsPostFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CsPostFileRepository extends JpaRepository<CsPostFile, Long> {
}