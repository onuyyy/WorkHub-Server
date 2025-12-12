package com.workhub.cs.service;

import com.workhub.global.notification.NotificationPublisher;
import com.workhub.project.entity.ProjectDevMember;
import com.workhub.project.service.ProjectService;
import com.workhub.projectNotification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CS 게시글 알림 전담 서비스.
 */
@Service
@RequiredArgsConstructor
public class CsPostNotificationService {

    private final NotificationPublisher notificationPublisher;
    private final ProjectService projectService;

    /**
     * CS 게시글 생성 시 개발사 멤버에게 알림.
     */
    public void notifyCsPostCreated(Long projectId, Long csPostId, String title) {
        Set<Long> receivers = findDevMembers(projectId);
        if (receivers.isEmpty()) return;
        String url = "/api/v1/projects/" + projectId + "/csPosts";
        notificationPublisher.publishCsPost(receivers, NotificationType.CS_POST_CREATED,
                title, "CS 게시물이 생성되었습니다.", url, csPostId);
    }

    /**
     * CS 게시글 수정 시 개발사 멤버 + 해당 게시물의 댓글 작성자에게 알림.
     */
    public void notifyCsPostUpdated(Long projectId, Long csPostId, String title, Set<Long> commenters) {
        Set<Long> receivers = findDevMembers(projectId);
        receivers.addAll(commenters);
        if (receivers.isEmpty()) return;
        String url = "/api/v1/projects/" + projectId + "/csPosts";
        notificationPublisher.publishCsPost(receivers, NotificationType.CS_POST_UPDATED,
                title, "CS 게시물이 수정되었습니다.", url, csPostId);
    }

    private Set<Long> findDevMembers(Long projectId) {
        List<ProjectDevMember> devs = projectService.getDevMemberByProjectIdIn(List.of(projectId));
        Set<Long> ids = new HashSet<>();
        devs.forEach(d -> ids.add(d.getUserId()));
        return ids;
    }
}
