package com.workhub.projectNotification.api;

import com.workhub.global.response.ApiResponse;
import com.workhub.projectNotification.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "Notification", description = "프로젝트 알림 API")
@RequestMapping("/api/v1/notifications")
public interface NotificationApi {

    @Operation(
            summary = "알림 SSE 스트림 구독",
            description = "Last-Event-ID로 끊긴 알림을 보충하고, 신규 알림을 SSE로 수신합니다.",
            parameters = {
                    @Parameter(name = "Last-Event-ID", in = ParameterIn.HEADER, required = false, description = "마지막으로 수신한 이벤트 ID (재연결 시 누락 알림 보충용)")
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "SSE 스트림 연결 성공",
                    content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter stream(@RequestHeader(value = "Last-Event-ID", required = false) Long lastEventId);

    @Operation(summary = "알림 목록 조회", description = "최신 알림 목록(예: 최대 50개)을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "알림 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = NotificationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<List<NotificationResponse>>> list();

    @Operation(summary = "미읽음 카운트 조회", description = "미읽음 알림 개수를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "미읽음 카운트 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Long.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping(value = "/unread-count", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Long>> unreadCount();

    @Operation(
            summary = "알림 읽음 처리",
            description = "지정한 알림을 읽음 상태로 변경합니다.",
            parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, description = "알림 식별자", required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "알림 읽음 처리 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping(value = "/{id}/read", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id);
}
