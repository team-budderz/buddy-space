package team.budderz.buddyspace.domain.chat.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.request.ws.ChatMessageSendRequest;
import team.budderz.buddyspace.api.chat.response.ws.ChatMessageResponse;
import team.budderz.buddyspace.api.chat.response.ws.ReadReceiptResponse;
import team.budderz.buddyspace.domain.chat.exception.ChatErrorCode;
import team.budderz.buddyspace.domain.chat.exception.ChatException;
import team.budderz.buddyspace.domain.chat.validator.ChatValidator;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.infra.database.chat.entity.ChatMessage;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;
import team.budderz.buddyspace.infra.database.chat.entity.MessageType;
import team.budderz.buddyspace.infra.database.chat.repository.ChatMessageRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatRoomRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 메시지 처리 서비스입니다.
 * 메시지 저장, 삭제, 유저 및 방 검증 등의 책임을 가집니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;
    private final ChatValidator chatValidator;

    /**
     * 클라이언트로부터 수신한 메시지를 저장합니다.
     *
     * @param request 메시지 전송 요청 DTO
     * @return 저장된 메시지 응답 DTO
     */
    @Operation(summary = "채팅 메시지 저장", description = "사용자가 보낸 채팅 메시지를 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 저장 성공")
    })
    public ChatMessageResponse saveChatMessage(ChatMessageSendRequest request) {
        // 채팅방 존재 + 유저 확인 및 조회
        ChatRoom chatRoom = getChatRoomOrThrow(request.roomId());
        User sender = getUserOrThrow(request.senderId());

        // 채팅방 참여자인지 검증
        assertParticipantOrThrow(sender.getId(), chatRoom);

        // 메시지 저장
        ChatMessage chatMessage = buildChatMessage(request, chatRoom, sender);
        chatMessageRepository.save(chatMessage);

        return toChatMessageResponse(chatMessage);
    }

    /**
     * 채팅방 ID를 통해 채팅방을 조회하고 없으면 예외를 던집니다.
     *
     * @param roomId 채팅방 ID
     * @return 채팅방 엔티티
     */
    private ChatRoom getChatRoomOrThrow(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    /**
     * 채팅 메시지를 삭제합니다.
     *
     * @param roomId 채팅방 ID
     * @param messageId 메시지 ID
     * @param userId 요청자 ID
     */
    @Operation(summary = "채팅 메시지 삭제", description = "요청자가 보낸 메시지를 삭제합니다. (5분 내 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 삭제 성공")
    })
    public void deleteMessageByRoom(Long roomId, Long messageId, Long userId) {
        chatValidator.validateParticipant(roomId, userId);

        // 메시지 조회·검증
        ChatMessage msg = chatValidator.validateMessageInRoom(messageId, roomId);

        // 본인 + 5분 검증
        if (!msg.getSender().getId().equals(userId)) {
            throw new ChatException(ChatErrorCode.NO_PERMISSION);
        }
        if (msg.getSentAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new ChatException(ChatErrorCode.MESSAGE_DELETE_TIME_EXPIRED);
        }

        chatMessageRepository.delete(msg);
    }

    /**
     * 사용자 ID로 유저를 조회하고 없으면 예외를 던집니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     */
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND));
    }

    /**
     * 채팅방 참여 여부 검증
     *
     * @param userId 사용자 ID
     * @param chatRoom 채팅방 엔티티
     */
    private void assertParticipantOrThrow(Long userId, ChatRoom chatRoom) {
        boolean exists = chatParticipantRepository
                .findByUserAndChatRoom_IdAndChatRoom_Group_IdAndIsActiveTrue(
                        userId,
                        chatRoom.getId(),
                        chatRoom.getGroup().getId()
                )
                .isPresent();
        if (!exists) {
            throw new ChatException(ChatErrorCode.USER_NOT_IN_CHAT_ROOM);
        }
    }

    /**
     * 메시지 저장용 엔티티 생성
     *
     * @param request 메시지 요청
     * @param chatRoom 채팅방
     * @param sender 보낸 유저
     * @return ChatMessage 엔티티
     */
    private ChatMessage buildChatMessage(ChatMessageSendRequest request, ChatRoom chatRoom, User sender) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageType(MessageType.valueOf(request.messageType()))
                .content(request.content())
                .attachmentUrl(request.attachmentUrl())
                .sentAt(LocalDateTime.now())
                .build();
    }

    /**
     * 엔티티를 응답 DTO 로 변환합니다.
     *
     * @param chatMessage 채팅 메시지
     * @return ChatMessageResponse DTO
     */
    private ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getSender().getId(),
                chatMessage.getMessageType().name(),
                chatMessage.getContent(),
                chatMessage.getAttachmentUrl(),
                chatMessage.getSentAt()
        );
    }

}
