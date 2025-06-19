package team.budderz.buddyspace.api.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.chat.request.common.ReadEvent;
import team.budderz.buddyspace.api.chat.request.rest.ReadStatusRestRequest;
import team.budderz.buddyspace.domain.chat.service.ChatReadService;
import team.budderz.buddyspace.global.security.UserAuth;

/* HTTP 기반 읽음 상태 저장 & HTTP → WS 브로드캐스트 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group/{groupId}/chat/rooms")
public class ChatReadController {

    private final ChatReadService chatReadService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /** 실시간 단건 갱신 */
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
