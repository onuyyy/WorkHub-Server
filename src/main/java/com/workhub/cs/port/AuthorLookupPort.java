package com.workhub.cs.port;

import com.workhub.cs.port.dto.AuthorProfile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CS 모듈이 외부 사용자 정보를 조회하기 위한 포트.
 */
public interface AuthorLookupPort {

    Optional<AuthorProfile> findByUserId(Long userId);

    Map<Long, AuthorProfile> findByUserIds(List<Long> userIds);
}
