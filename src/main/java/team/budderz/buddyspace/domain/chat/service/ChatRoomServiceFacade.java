package team.budderz.buddyspace.domain.chat.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.budderz.buddyspace.api.chat.request.rest.AddParticipantRequest;
import team.budderz.buddyspace.api.chat.request.rest.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.request.rest.UpdateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.rest.*;

import java.util.List;

/**
 * 채팅방 Command/Query 분리용 퍼사드 서비스
 * 트랜잭션 경계 관리 및 API 단위의 일관성 보장
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomServiceFacade {

    private final ChatRoomCommandService chatRoomCommandService;
    private final ChatRoomQueryService chatRoomQueryService;

    /** 채팅방 생성 (쓰기 트랜잭션) */
    @Transactional
    public CreateChatRoomResponse createChatRoom(Long groupId, Long userId, CreateChatRoomRequest request) {
        return chatRoomCommandService.createChatRoom(groupId, userId, request);
    }

    /** 채팅방 수정 */
    @Transactional
    public UpdateChatRoomResponse updateChatRoom(
            Long groupId,
            Long roomId,
            Long userId,
            UpdateChatRoomRequest req
    ) {
        return chatRoomCommandService.updateChatRoom(groupId, roomId, userId, req);
    }

    /** 채팅방 삭제 */
    @Transactional
    public void deleteChatRoom(Long groupId, Long roomId, Long userId) {
        chatRoomCommandService.deleteChatRoom(groupId, roomId, userId);
    }

    /** 내 채팅방 목록 조회 */
    public List<ChatRoomSummaryResponse> getMyChatRooms(Long groupId, Long userId) {
        return chatRoomQueryService.getMyChatRooms(groupId, userId);
    }

    /** 채팅방 입장 후 과거 메시지 조회 */
    public GetChatMessagesResponse getChatMessages(Long groupId, Long roomId, Long userId, int page, int size) {
        return chatRoomQueryService.getChatMessages(groupId, roomId, userId, page, size);
    }

    /** 채팅방 멤버 목록 조회  */
    public List<ChatRoomMemberResponse> getChatRoomMembers(Long groupId, Long roomId, Long userId) {
        return chatRoomQueryService.getChatRoomMembers(groupId, roomId, userId);
    }

    /** 단일 방 조회  */
    public ChatRoomDetailResponse getChatRoomDetail(Long groupId, Long roomId, Long userId) {
        return chatRoomQueryService.getChatRoomDetail(groupId, roomId, userId);
    }

    /** 참여자 추가 */
    @Transactional
    public void addParticipant(
            Long groupId,
            Long roomId,
            Long userId,
            AddParticipantRequest req
    ) {
        chatRoomCommandService.addParticipant(groupId, roomId, userId, req);
    }

    /** 참여자 제거 */
    @Transactional
    public void removeParticipant(
            Long groupId,
            Long roomId,
            Long userId,
            Long targetUserId
    ) {
        chatRoomCommandService.removeParticipant(groupId, roomId, userId, targetUserId);
    }

    /** 읽음 상태 조회 */
    public ReadStatusRestResponse getReadStatus(Long groupId, Long roomId, Long userId) {
        return chatRoomQueryService.getReadStatus(groupId, roomId, userId);
    }
}
