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

    /* 실시간 단건 갱신 (채팅창 맨 아래 볼 때마다) */
    public void markAsRead(Long roomId, Long userId, Long messageId) {
        ChatParticipant cp = chatParticipantRepository
                .findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("참가자 정보 없음"));

        cp.updateLastRead(messageId);
    }

    /* 일괄 동기화 (재접속·무한스크롤 시) */
    public void syncReadPointer(Long roomId, Long userId, Long lastId) {
        ChatParticipant cp = chatParticipantRepository
                .findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("참가자 정보 없음"));

        cp.syncLastRead(lastId);
    }
}

