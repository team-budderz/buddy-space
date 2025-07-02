package team.budderz.buddyspace.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.response.rest.ChatRoomMemberResponse;
import team.budderz.buddyspace.domain.user.provider.UserProfileImageProvider;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;

import java.util.List;

/**
 * 채팅방 멤버 관련 이벤트(WebSocket 브로드캐스트)를 처리하는 서비스입니다.
 * <p>
 * 사용자가 채팅방에 입장하거나 퇴장, 초대, 강퇴 등의 변동이 있을 경우,
 * 실시간으로 현재 참여자 목록을 WebSocket 구독자에게 전송합니다.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ChatMemberEventService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserProfileImageProvider profileImageProvider;

    /**
     * 채팅방의 현재 활성화된 참여자 목록을 조회하고,
     * WebSocket 채널(`/sub/chat/rooms/{roomId}/members`)로 브로드캐스트합니다.
     *
     * <p>다음과 같은 상황에서 호출됩니다:
     * <ul>
     *     <li>채팅방에 새 참여자 초대</li>
     *     <li>기존 참여자 강퇴</li>
     *     <li>참여자 나가기</li>
     * </ul>
     *
     * @param roomId 대상 채팅방 ID
     */
    public void broadcastMembers(Long roomId) {
        // 1. 현재 멤버 리스트 추출
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoomId(roomId);
        List<ChatRoomMemberResponse> memberList = participants.stream()
                .filter(ChatParticipant::isActive)
                .map(cp -> new ChatRoomMemberResponse(
                        cp.getUser().getId(),
                        cp.getUser().getName(),
                        profileImageProvider.getProfileImageUrl(cp.getUser())
                ))
                .toList();

        // 2. WebSocket 채널로 전체 전송
        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + roomId + "/members",
                memberList
        );
    }
}
