package team.budderz.buddyspace.api.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import team.budderz.buddyspace.api.chat.request.ChatMessageSendRequest;
import team.budderz.buddyspace.api.chat.response.ChatMessageResponse;
import team.budderz.buddyspace.domain.chat.service.ChatMessageService;

// WebSocket: 실시간 채팅
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate; // broadcast 용

    // 클라이언트 → 서버: /pub/chat/message
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageSendRequest request) {
        // 메시지 저장
        ChatMessageResponse savedMessage = chatMessageService.saveChatMessage(request);

        // 서버 → 클라이언트 broadcast: /sub/chat/room/{roomId}
        String destination = "/sub/chat/room/" + request.roomId();
        messagingTemplate.convertAndSend(destination, savedMessage);
    }
}
