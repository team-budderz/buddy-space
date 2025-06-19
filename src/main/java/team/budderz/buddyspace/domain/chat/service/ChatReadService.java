package team.budderz.buddyspace.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.response.ws.ReadReceiptResponse;
import team.budderz.buddyspace.domain.chat.exception.ChatErrorCode;
import team.budderz.buddyspace.domain.chat.exception.ChatException;
import team.budderz.buddyspace.domain.chat.validator.ChatValidator;
import team.budderz.buddyspace.infra.database.chat.entity.ChatMessage;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatReadService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatValidator chatValidator;

    /* 실시간 단건 갱신 (채팅창 맨 아래 볼 때마다) */
    public void markAsRead(Long roomId, Long userId, Long messageId) {
        // 1. 존재 여부/관계 검증
        ChatMessage message = chatValidator.validateMessageInRoom(messageId, roomId);
        ChatParticipant participant = chatValidator.validateParticipant(roomId, userId);

        // 2. 권한 검증
        chatValidator.validateReadPermission(roomId, userId, message);

        // 3. 네트워크 지연 예방 (lastReadMessageId < messageId 인 경우만 갱신)
        if (messageId > participant.getLastReadMessageId()) {
            participant.updateLastRead(messageId);
        }
    }

    /* 일괄 동기화 (재접속·무한스크롤 시) */
    public void syncReadPointer(Long roomId, Long userId, Long lastId) {
        ChatParticipant participant = chatValidator.validateParticipant(roomId, userId);
        participant.syncLastRead(lastId);
    }

    /**
     * 사용자가 메시지 읽음 처리
     */
    @Transactional
    public void updateLastRead(Long roomId, Long userId, Long lastReadMessageId) {
        int updated = chatParticipantRepository.updateLastRead(roomId, userId, lastReadMessageId);
        if (updated == 0) {
            throw new ChatException(ChatErrorCode.USER_NOT_IN_CHAT_ROOM);
        }
    }

    /**
     * 해당 방의 모든 참가자별 마지막 읽음 ID 조회
     */
    @Transactional(readOnly = true)
    public List<ReadReceiptResponse> getAllReads(Long roomId) {
        return chatParticipantRepository.findActiveByRoom(roomId).stream()
                .map(p -> new ReadReceiptResponse(p.getUser().getId(), p.getLastReadMessageId()))
                .toList();
    }
}

