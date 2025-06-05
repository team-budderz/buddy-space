package team.budderz.buddyspace.api.chat.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomSummaryResponse {
    private Long roomId;
    private String name;
    private String lastMessage;
    private String lastMessageType;
    private LocalDateTime lastMessageTime;
    private long unreadCount;
}

