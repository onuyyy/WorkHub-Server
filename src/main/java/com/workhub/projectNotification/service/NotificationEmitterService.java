package com.workhub.projectNotification.service;

import com.workhub.projectNotification.dto.NotificationResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationEmitterService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    /**
     * 구독 등록: emitter를 사용자 ID로 저장하고, 타임아웃/에러 시 정리한다.
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(600_000L);
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));
        return emitter;
    }
    /**
     * 알림 DTO를 SSE로 즉시 전송.
     * 연결이 없으면 무시(서버는 저장만 되어 있으므로 재연결 시 lastEventId로 보충 가능).
     */
    public void send(Long userId, NotificationResponse payload) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(payload.id()))      // event id = 알림 PK
                    .name(payload.type().name())           // event name = 알림 타입
                    .data(payload));
        } catch (IOException ex) {
            // 전송 실패 시 emitter를 제거하여 깨끗이 정리
            emitters.remove(userId);
        }
    }

    /**
     * 주기적으로 ping 이벤트를 보내 연결을 유지한다.
     */
    @Scheduled(fixedRate = 30_000L)
    public void sendKeepAlive() {
        if (emitters.isEmpty()) {
            return;
        }
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("ping")
                        .data("keep-alive"));
            } catch (IOException ex) {
                emitters.remove(userId);
            }
        });
    }
}
