package team.budderz.buddyspace.api.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.chat.request.common.ReadEvent;
import team.budderz.buddyspace.api.chat.request.rest.ReadStatusRestRequest;
import team.budderz.buddyspace.domain.chat.service.ChatReadService;
import team.budderz.buddyspace.global.security.UserAuth;

/**
 * 채팅방의 읽음 상태를 HTTP 로 저장하고, WebSocket 으로 읽음 정보를 브로드캐스트하는 컨트롤러입니다.
 */
@Tag(name = "채팅 읽음 상태", description = "읽음 상태 저장 및 브로드캐스트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group/{groupId}/chat/rooms")
public class ChatReadController {

    private final ChatReadService chatReadService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 읽음 상태를 서버에 저장하고 WebSocket 을 통해 구독자들에게 읽음 이벤트를 브로드캐스트합니다.
     *
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     * @param body 읽음 정보 요청 객체 (lastReadMessageId 포함)
     * @param userAuth 인증된 사용자 정보
     */
    @Operation(
            summary = "읽음 상태 저장 및 브로드캐스트",
            description = "읽은 메시지 ID를 저장하고, 해당 정보를 구독 중인 클라이언트에게 실시간 전송합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "읽음 상태 저장 및 브로드캐스트 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "404", description = "채팅방 또는 메시지를 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping("/{roomId}/read-status")
    public void read(
            @PathVariable Long groupId,
            @PathVariable Long roomId,
            @RequestBody ReadStatusRestRequest body,
            @AuthenticationPrincipal UserAuth userAuth
    ) {

        Long userId = userAuth.getUserId();
        chatReadService.markAsRead(roomId, userId, body.lastReadMessageId());

        // WS 구독자에게만 읽음 이벤트 발송
        simpMessagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + roomId + "/read",
                new ReadEvent(userId, body.lastReadMessageId())
        );
    }

}
