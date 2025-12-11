package com.workhub.projectNotification.repository;

import com.workhub.projectNotification.entity.ProjectNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectNotificationRepository extends JpaRepository<ProjectNotification, Long> {
    List<ProjectNotification> findTop50ByUserIdOrderByProjectNotificationIdDesc(Long userId);
    List<ProjectNotification> findByUserIdAndProjectNotificationIdGreaterThanOrderByProjectNotificationIdAsc(Long userId, Long lastEventId);
    long countByUserIdAndReadAtIsNull(Long userId);

    Optional<ProjectNotification> findByProjectNotificationIdAndUserId(Long id, Long userId);
}
