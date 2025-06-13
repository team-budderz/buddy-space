package team.budderz.buddyspace.api.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import team.budderz.buddyspace.api.chat.request.ChatMessageSendRequest;
import team.budderz.buddyspace.api.chat.response.ChatMessageResponse;
import team.budderz.buddyspace.domain.chat.service.ChatMessageService;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

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
            String destination = "/sub/chat/room/" + completedRequest.roomId();
            messagingTemplate.convertAndSend(destination, savedMessage);

        } catch (Exception e) {
            sendErrorToClient("메시지 전송에 실패했습니다: " + e.getMessage(), senderId);
        }
    }

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
