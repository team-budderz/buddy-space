package team.budderz.buddyspace.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatReadService {

    private final ChatParticipantRepository chatParticipantRepository;

    /** 마지막 읽음 메시지 ID 갱신 */
    public void updateLastRead(Long roomId, Long userId, Long messageId) {
        ChatParticipant chatParticipant = chatParticipantRepository
                .findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("참가자 정보 없음"));

        chatParticipant.updateLastRead(messageId);
        // JPA dirty-checking 으로 자동 flush
    }
}

