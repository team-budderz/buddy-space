package team.budderz.buddyspace.domain.chat.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

/**
 * 채팅방 읽음 처리에 대한 비즈니스 로직을 담당하는 서비스입니다.
 * - 메시지 읽음 마킹
 * - 읽음 포인터 동기화
 * - 읽음 상태 조회
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChatReadService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatValidator chatValidator;

    /**
     * 사용자가 특정 메시지를 읽었음을 표시합니다.
     * - messageId가 기존 lastReadMessageId 보다 큰 경우만 업데이트됩니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @param messageId 읽은 메시지 ID
     */
    @Operation(summary = "메시지 읽음 처리", description = "특정 메시지를 사용자가 읽었음을 서버에 반영합니다. 네트워크 지연을 고려하여 더 최신 메시지만 반영됩니다.")
    @ApiResponse(responseCode = "200", description = "읽음 상태 업데이트 성공")
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

    /**
     * 클라이언트가 재접속하거나 메시지를 스크롤할 때
     * 마지막 읽은 메시지 ID를 강제로 동기화합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @param lastId 클라이언트가 최종적으로 읽은 메시지 ID
     */
    @Operation(summary = "읽음 포인터 동기화", description = "재접속 또는 스크롤 시 클라이언트가 마지막으로 읽은 메시지 ID를 서버에 동기화합니다.")
    @ApiResponse(responseCode = "200", description = "동기화 성공")
    public void syncReadPointer(Long roomId, Long userId, Long lastId) {
        ChatParticipant participant = chatValidator.validateParticipant(roomId, userId);
        participant.syncLastRead(lastId);
    }

    /**
     * 사용자별로 채팅방에서 가장 마지막으로 읽은 메시지 ID를 DB에 업데이트합니다.
     * (단건 업데이트용, WebSocket 경유 처리)
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @param lastReadMessageId 최종 읽은 메시지 ID
     * @throws ChatException 해당 유저가 채팅방에 없을 경우 예외 발생
     */
    @Operation(summary = "읽음 포인터 갱신", description = "WebSocket 경유 시, 특정 사용자의 마지막 읽은 메시지 ID를 DB에 반영합니다.")
    @ApiResponse(responseCode = "200", description = "읽음 포인터 갱신 성공")
    @Transactional
    public void updateLastRead(Long roomId, Long userId, Long lastReadMessageId) {
        int updated = chatParticipantRepository.updateLastRead(roomId, userId, lastReadMessageId);
        if (updated == 0) {
            throw new ChatException(ChatErrorCode.USER_NOT_IN_CHAT_ROOM);
        }
    }

    /**
     * 채팅방 내 모든 참여자의 마지막 읽음 메시지 ID를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @return 참여자 ID와 마지막 읽은 메시지 ID 목록
     */
    @Transactional(readOnly = true)
    @Operation(summary = "채팅방 읽음 상태 조회", description = "채팅방 내 모든 사용자들의 마지막 읽은 메시지 ID를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public List<ReadReceiptResponse> getAllReads(Long roomId) {
        return chatParticipantRepository.findActiveByRoom(roomId).stream()
                .map(p -> new ReadReceiptResponse(p.getUser().getId(), p.getLastReadMessageId()))
                .toList();
    }
}

