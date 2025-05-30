package team.budderz.buddyspace.api.chat.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateChatRoomRequest {

    private String name; // 채팅방 이름
    private String description;
    private String chatRoomType;  // GROUP or DIRECT
    private List<Long> participantIds;  // 참여자 유저 ID 목록

}
