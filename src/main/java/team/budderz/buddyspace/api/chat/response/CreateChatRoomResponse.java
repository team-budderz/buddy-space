package team.budderz.buddyspace.api.chat.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateChatRoomResponse {
    private String roomId;   // 생성된 방 ID
    private String status;   // ex. success
}
