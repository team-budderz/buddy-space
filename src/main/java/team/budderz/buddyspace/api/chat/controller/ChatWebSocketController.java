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

// WebSocket: 실시간 채팅
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate; // broadcast 용

    // 클라이언트 → 서버: /pub/chat/message
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageSendRequest request, MessageHeaders headers) {
        try {
            // WebSocket 세션에서 userId 꺼내기
            Map<String, Object> sessionAttributes =
                    SimpMessageHeaderAccessor.getSessionAttributes(headers);

            Long senderId = (Long) sessionAttributes.get("userId");
            if (senderId == null) throw new RuntimeException("인증된 사용자 아님");

            // 이 senderId를 request 에 주입하거나 ChatMessageService 에 넘기기
            // 클라이언트는 senderId 없이 메시지를 보내고, 서버는 토큰을 기반으로 userId를 추출해서 쓰는 구조가 이상적
            ChatMessageSendRequest completedRequest = new ChatMessageSendRequest(
                    request.roomId(),
                    senderId, // 서버에서 넣어주는 값
                    request.messageType(),
                    request.content(),
                    request.attachmentUrl()
            );

            ChatMessageResponse savedMessage = chatMessageService.saveChatMessage(completedRequest);

            // 서버 → 클라이언트 broadcast: /sub/chat/room/{roomId}
            String destination = "/sub/chat/room/" + completedRequest.roomId();

            messagingTemplate.convertAndSend(destination, savedMessage);
        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                    "anonymous", // senderId가 없으니 안전하게 처리
                    "/queue/errors",
                    "메시지 전송에 실패했습니다: " + e.getMessage()
            );
        }
    }
}
