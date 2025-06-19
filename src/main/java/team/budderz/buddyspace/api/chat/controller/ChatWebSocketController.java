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
import team.budderz.buddyspace.api.chat.request.ws.ReadReceiptRequest;
import team.budderz.buddyspace.api.chat.response.ws.ChatMessageResponse;
import team.budderz.buddyspace.api.chat.response.ws.ReadReceiptResponse;
import team.budderz.buddyspace.domain.chat.service.ChatMessageService;
import team.budderz.buddyspace.domain.chat.service.ChatReadService;

import java.util.List;
import java.util.Map;

/* STOMP 메시지(메시지 전송, 읽음 Ack, 동기화) 처리 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final ChatReadService chatReadService;
    private final SimpMessagingTemplate messagingTemplate;

    /* 메시지 보내기 */
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
            String destination = "/sub/chat/rooms/" + completedRequest.roomId();
            messagingTemplate.convertAndSend(destination, savedMessage);

        } catch (Exception e) {
            sendErrorToClient("메시지 전송에 실패했습니다: " + e.getMessage(), senderId);
        }
    }

    /** 읽음(report read) 처리 후 브로드캐스트 */
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

    /** 재접속/동기화용 읽음 상태 요청 처리 */
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
