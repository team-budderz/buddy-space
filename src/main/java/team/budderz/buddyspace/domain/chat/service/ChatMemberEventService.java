package team.budderz.buddyspace.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.response.rest.ChatRoomMemberResponse;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ChatMemberEventService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /** 방의 멤버 변동(입장/퇴장/초대/강퇴) 발생 시 호출 */
    public void broadcastMembers(Long roomId) {
        // 1. 현재 멤버 리스트 추출
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoomId(roomId);
        List<ChatRoomMemberResponse> memberList = participants.stream()
                .filter(ChatParticipant::isActive)
                .map(cp -> new ChatRoomMemberResponse(
                        cp.getUser().getId(),
                        cp.getUser().getName(),
                        cp.getUser().getImageUrl()
                ))
                .toList();

        // 2. WebSocket 채널로 전체 전송
        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + roomId + "/members",
                memberList
        );
    }
}
