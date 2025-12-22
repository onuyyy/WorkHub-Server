package com.workhub.userTable.repository;

import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserTable, Long>, UserRepositoryCustom {
    Optional<UserTable> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    Optional<UserTable> findByEmail(String email);

    Long countByStatus(Status status);
}
