package com.workhub.userTable.repository;

import com.workhub.userTable.entity.UserTable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface UserRepositoryCustom {
    Map<Long, UserTable> findMapByUserIdIn(List<Long> userIds);
    List<UserTable> findMapByCompanyIdIn(Long companyId);
    Long countActiveUsersUntil(LocalDateTime until);
}
