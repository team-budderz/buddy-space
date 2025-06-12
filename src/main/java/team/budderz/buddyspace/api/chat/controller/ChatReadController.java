package team.budderz.buddyspace.api.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import team.budderz.buddyspace.api.chat.request.ReadEvent;
import team.budderz.buddyspace.api.chat.request.ReadRequest;
import team.budderz.buddyspace.domain.chat.service.ChatReadService;
import team.budderz.buddyspace.global.security.WebSocketPrincipal; // ↔ 핸드셰이크 인터셉터에서 세팅

@Controller
@RequiredArgsConstructor
public class ChatReadController {

    private final ChatReadService readService;
    private final SimpMessagingTemplate template;

    /** 실시간 단건 갱신  */
    @MessageMapping("/chat/rooms/{roomId}/read")
    public void read(@DestinationVariable Long roomId,
                     ReadRequest body,
                     WebSocketPrincipal principal) {

        Long userId = principal.userId();

        // DB 갱신
        readService.markAsRead(roomId, userId, body.lastReadMessageId());

        // 모든 참가자에게 브로드캐스트
        template.convertAndSend(
                "/sub/chat/rooms/" + roomId + "/read",
                new ReadEvent(userId, body.lastReadMessageId())
        );
    }

    /** 재접속/무한스크롤 일괄 Sync ──────────────── */
    @MessageMapping("/chat/rooms/{roomId}/read-sync")
    public void sync(@DestinationVariable Long roomId,
                     ReadRequest body,
                     WebSocketPrincipal principal){

        Long userId = principal.userId();
        readService.syncReadPointer(roomId, userId, body.lastReadMessageId());

        template.convertAndSend("/sub/chat/rooms/" + roomId + "/read",
                new ReadEvent(userId, body.lastReadMessageId()));
    }

}
