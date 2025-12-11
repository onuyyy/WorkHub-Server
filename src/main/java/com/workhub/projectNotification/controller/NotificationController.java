package com.workhub.projectNotification.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.global.util.SecurityUtil;
import com.workhub.projectNotification.api.NotificationApi;
import com.workhub.projectNotification.dto.NotificationResponse;
import com.workhub.projectNotification.service.ProjectNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final ProjectNotificationService notificationService;

    /**
     * SSE 구독 엔드포인트.
     * Last-Event-ID 헤더를 이용해 재연결 시 누락 알림을 보충한다.
     */
    @GetMapping("/stream")
    public SseEmitter stream(@RequestHeader(value = "Last-Event-ID", required = false) Long lastEventId) {
        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
        return notificationService.subscribe(userId, lastEventId);
    }

    /**
     * 최신 알림 목록 반환(최대 50개 예시).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> list() {
        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
        return ApiResponse.success(notificationService.listRecent(userId));
    }

    /**
     * 미읽음 카운트 조회.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> unreadCount() {
        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
        return ApiResponse.success(notificationService.unreadCount(userId));
    }

    /**
     * 읽음 처리.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
        notificationService.markRead(userId, id);
        return ApiResponse.success(null);
    }
}
