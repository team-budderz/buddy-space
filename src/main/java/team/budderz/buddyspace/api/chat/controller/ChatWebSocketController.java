package team.budderz.buddyspace.api.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.request.ws.ChatMessageSendRequest;
import team.budderz.buddyspace.api.chat.request.ws.DeleteMessageRequest;
import team.budderz.buddyspace.api.chat.request.ws.ReadReceiptRequest;
import team.budderz.buddyspace.api.chat.response.ws.ChatMessageResponse;
import team.budderz.buddyspace.api.chat.response.ws.ReadReceiptResponse;
import team.budderz.buddyspace.domain.chat.service.ChatMessageService;
import team.budderz.buddyspace.domain.chat.service.ChatReadService;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 기반의 채팅 메시지 전송, 삭제, 읽음 처리, 동기화 기능을 담당하는 컨트롤러입니다.
 * 클라이언트는 STOMP 프로토콜을 통해 서버와 통신하며,
 * /pub 경로로 메시지를 전송하고 /sub 경로를 통해 수신합니다.
 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final ChatReadService chatReadService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 클라이언트가 전송한 채팅 메시지를 저장하고, 해당 채팅방의 구독자들에게 브로드캐스트합니다.
     * STOMP 경로: <br>
     * → `/pub/chat/message`<br>
     * 브로드캐스트 경로: <br>
     * → `/sub/chat/rooms/{roomId}/messages`
     */
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageSendRequest request, MessageHeaders headers) {
        Long senderId = extractSenderId(headers);

        if (senderId == null) {
            sendErrorToClient("인증된 사용자가 아닙니다.", null);
            return;
        }

        // 서버에서 senderId를 주입
        ChatMessageSendRequest completedRequest = buildRequestWithSender(request, senderId);

        try {
            ChatMessageResponse savedMessage = chatMessageService.saveChatMessage(completedRequest);

            // Broadcast
            String destination = "/sub/chat/rooms/" + completedRequest.roomId() + "/messages";
            messagingTemplate.convertAndSend(destination, savedMessage);

        } catch (Exception e) {
            sendErrorToClient("메시지 전송에 실패했습니다: " + e.getMessage(), senderId);
        }
    }

    /**
     * 읽음 처리 요청을 받아 DB에 반영하고, 해당 방 구독자에게 읽음 이벤트를 브로드캐스트합니다.
     * STOMP 경로: `/pub/chat/rooms/{roomId}/read` <br>
     * 브로드캐스트: `/sub/chat/rooms/{roomId}/read`
     */
    @MessageMapping("/chat/rooms/{roomId}/read")
    @Transactional
    public void handleRead(
            @DestinationVariable Long roomId,
            ReadReceiptRequest payload,
            MessageHeaders headers
    ) {
        Long userId = extractSenderId(headers);
        if (userId == null) return;

        chatReadService.updateLastRead(roomId, userId, payload.lastReadMessageId());

        // 브로드캐스트할 페이로드 구성
        ReadReceiptResponse resp = new ReadReceiptResponse(userId, payload.lastReadMessageId());
        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + roomId + "/read",
                resp
        );
    }

    /**
     * 재접속 또는 초기화 시, 채팅방의 전체 읽음 상태를 요청하면 클라이언트들에게 전송합니다.
     * STOMP 경로: `/pub/chat/rooms/{roomId}/read-sync` <br>
     * 브로드캐스트: `/sub/chat/rooms/{roomId}/read-sync`
     */
    @MessageMapping("/chat/rooms/{roomId}/read-sync")
    public void handleReadSync(
            @DestinationVariable Long roomId,
            ReadReceiptRequest req,
            MessageHeaders headers
    ) {
        Long userId = extractSenderId(headers);
        if (userId == null) return;

        List<ReadReceiptResponse> allReads = chatReadService.getAllReads(roomId);

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + roomId + "/read-sync",
                allReads
        );
    }

    /**
     * 사용자가 보낸 채팅 메시지를 삭제하고 삭제 이벤트를 해당 방 구독자들에게 전송합니다.
     * STOMP 경로: `/pub/chat/rooms/{roomId}/delete` <br>
     * 브로드캐스트: `/sub/chat/rooms/{roomId}/messages`
     */
    @MessageMapping("/chat/rooms/{roomId}/delete")
    @Transactional
    public void deleteMessageWs(
            @DestinationVariable Long roomId,
            DeleteMessageRequest payload,
            MessageHeaders headers
    ) {
        Long userId    = extractSenderId(headers);
        Long messageId = payload.messageId();

        try {

            // WS 전용 삭제
        chatMessageService.deleteMessageByRoom(roomId, messageId, userId);

        // 삭제 이벤트 브로드캐스트 (클라이언트가 이걸 받아서 li 제거)
        Map<String,Object> event = Map.of(
                "event","message:delete",
                "data", Map.of(
                        "roomId",    roomId,
                        "messageId", messageId,
                        "senderId",  userId
                )
        );
        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + roomId + "/messages",
                event
        );
        } catch (Exception e) {
            sendErrorToClient("메시지 삭제에 실패했습니다: " + e.getMessage(), userId);
        }
    }

    // ────────────────────────────────────────────────────────────────────────

    /** WebSocket 세션에서 senderId 추출 */
    private Long extractSenderId(MessageHeaders headers) {
        Map<String, Object> sessionAttributes = SimpMessageHeaderAccessor.getSessionAttributes(headers);
        if (sessionAttributes == null) return null;
        Object id = sessionAttributes.get("userId");
        if (id instanceof Long) return (Long) id;
        if (id instanceof Integer) return ((Integer) id).longValue();
        if (id instanceof String) {
            try { return Long.parseLong((String) id); } catch (NumberFormatException ignore) {}
        }
        return null;
    }

    /** senderId를 request 에 주입 */
    private ChatMessageSendRequest buildRequestWithSender(ChatMessageSendRequest origin, Long senderId) {
        return new ChatMessageSendRequest(
                origin.roomId(),
                senderId,
                origin.messageType(),
                origin.content(),
                origin.attachmentUrl()
        );
    }

    /** 에러 메시지 전송 */
    private void sendErrorToClient(String errorMsg, Long senderId) {
        // 유저별 에러 큐로 전송 - senderId 없으면 broadcast error topic 등 활용 가능
        String errorDestination = (senderId != null)
                ? "/queue/errors"      // Spring STOMP 표준 유저별 에러 큐
                : "/topic/chat/errors"; // fallback broadcast (선택)

        messagingTemplate.convertAndSend(errorDestination, errorMsg);
    }

}
