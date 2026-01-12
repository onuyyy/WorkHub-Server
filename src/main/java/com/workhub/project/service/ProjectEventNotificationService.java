package com.workhub.project.service;

import com.workhub.global.notification.NotificationPublisher;
import com.workhub.global.notification.NotificationTargetFinder;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import com.workhub.projectNotification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 프로젝트 도메인 알림 전담 서비스.
 * - 프로젝트 생성, 상태 변경 시 구성원 전체에게 알림을 발행한다.
 */
@Service
@RequiredArgsConstructor
public class ProjectEventNotificationService {

    private final NotificationPublisher notificationPublisher;
    private final NotificationTargetFinder targetFinder;

    /**
     * 프로젝트 생성 알림: 프로젝트에 속한 전원에게 발행.
     */
    public void notifyProjectCreated(Project project) {
        Set<Long> receivers = targetFinder.findAllMembersOfProject(project.getProjectId());
        String title = project.getProjectTitle();
        String content = "프로젝트가 생성되었습니다.";
        String relatedUrl = "/projects/" + project.getProjectId();

        notificationPublisher.publishProject(
                receivers,
                NotificationType.PROJECT_CREATED,
                title,
                content,
                relatedUrl,
                project.getProjectId()
        );
    }

    /**
     * 프로젝트 상태 변경 알림: 이전/현재 상태 정보를 포함하여 구성원 전체에게 발행.
     */
    public void notifyStatusChanged(Project project, Status previousStatus) {
        Set<Long> receivers = targetFinder.findAllMembersOfProject(project.getProjectId());
        String title = project.getProjectTitle();
        String content = String.format("프로젝트 상태가 %s → %s 로 변경되었습니다.",
                previousStatus, project.getStatus());
        String relatedUrl = "/projects/" + project.getProjectId();

        notificationPublisher.publishProject(
                receivers,
                NotificationType.STATUS_CHANGED,
                title,
                content,
                relatedUrl,
                project.getProjectId()
        );
    }

    /**
     * 프로젝트 멤버 추가 알림: 추가된 멤버가 있으면 전원에게 발행.
     */
    public void notifyMembersAdded(Project project, int addedCount) {
        if (addedCount <= 0) {
            return;
        }
        Set<Long> receivers = targetFinder.findAllMembersOfProject(project.getProjectId());
        String title = project.getProjectTitle();
        String content = String.format("프로젝트에 멤버 %d명이 추가되었습니다.", addedCount);
        String relatedUrl = "/projects/" + project.getProjectId();

        notificationPublisher.publishProject(
                receivers,
                NotificationType.PROJECT_MEMBER_ADDED,
                title,
                content,
                relatedUrl,
                project.getProjectId()
        );
    }

    /**
     * 프로젝트 멤버 삭제 알림: 제거된 멤버가 있으면 전원에게 발행.
     */
    public void notifyMembersRemoved(Project project, int removedCount) {
        if (removedCount <= 0) {
            return;
        }
        Set<Long> receivers = targetFinder.findAllMembersOfProject(project.getProjectId());
        String title = project.getProjectTitle();
        String content = String.format("프로젝트에서 멤버 %d명이 제거되었습니다.", removedCount);
        String relatedUrl = "/projects/" + project.getProjectId();

        notificationPublisher.publishProject(
                receivers,
                NotificationType.PROJECT_MEMBER_REMOVED,
                title,
                content,
                relatedUrl,
                project.getProjectId()
        );
    }

    /**
     * 프로젝트 기본 정보 변경 알림(제목/설명/종료일).
     */
    public void notifyInfoUpdated(Project project, boolean titleChanged, boolean descriptionChanged, boolean endDateChanged) {
        if (!(titleChanged || descriptionChanged || endDateChanged)) {
            return;
        }
        Set<Long> receivers = targetFinder.findAllMembersOfProject(project.getProjectId());
        String title = project.getProjectTitle();

        String changedFields = buildChangedFields(titleChanged, descriptionChanged, endDateChanged);
        String content = String.format("프로젝트 정보가 변경되었습니다. (%s)", changedFields);
        String relatedUrl = "/projects/" + project.getProjectId();

        notificationPublisher.publishProject(
                receivers,
                NotificationType.PROJECT_INFO_UPDATED,
                title,
                content,
                relatedUrl,
                project.getProjectId()
        );
    }

    private String buildChangedFields(boolean title, boolean desc, boolean endDate) {
        StringBuilder sb = new StringBuilder();
        if (title) sb.append("제목, ");
        if (desc) sb.append("설명, ");
        if (endDate) sb.append("종료일, ");
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2); // remove last comma+space
        }
        return sb.toString();
    }
}
