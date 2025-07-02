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

    /**
     * 채팅방을 생성합니다.
     *
     * @param groupId 그룹 ID
     * @param userId 생성자 ID
     * @param request 채팅방 생성 요청 DTO
     * @return 생성된 채팅방 정보
     */

    @Transactional
    public CreateChatRoomResponse createChatRoom(Long groupId, Long userId, CreateChatRoomRequest request) {
        return chatRoomCommandService.createChatRoom(groupId, userId, request);
    }

    /**
     * 채팅방의 이름 및 설명을 수정합니다.
     *
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     * @param userId 요청자 ID
     * @param req 수정 요청 DTO
     * @return 수정된 채팅방 정보
     */
    @Transactional
    public UpdateChatRoomResponse updateChatRoom(
            Long groupId,
            Long roomId,
            Long userId,
            UpdateChatRoomRequest req
    ) {
        return chatRoomCommandService.updateChatRoom(groupId, roomId, userId, req);
    }

    /**
     * 채팅방을 삭제합니다.
     *
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     * @param userId 삭제 요청자 ID
     */
    @Transactional
    public void deleteChatRoom(Long groupId, Long roomId, Long userId) {
        chatRoomCommandService.deleteChatRoom(groupId, roomId, userId);
    }

    /**
     * 현재 유저가 속한 채팅방 목록을 조회합니다.
     *
     * @param groupId 그룹 ID
     * @param userId 사용자 ID
     * @return 채팅방 요약 리스트
     */
    public List<ChatRoomSummaryResponse> getMyChatRooms(Long groupId, Long userId) {
        return chatRoomQueryService.getMyChatRooms(groupId, userId);
    }

    /**
     * 특정 채팅방의 과거 메시지를 페이징 조회합니다.
     *
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     * @param userId 요청자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 채팅 메시지 목록과 페이징 정보
     */
    public GetChatMessagesResponse getChatMessages(Long groupId, Long roomId, Long userId, int page, int size) {
        return chatRoomQueryService.getChatMessages(groupId, roomId, userId, page, size);
    }

    /**
     * 채팅방에 속한 멤버 목록을 조회합니다.
     *
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     * @param userId 요청자 ID
     * @return 참여자 목록
     */
    public List<ChatRoomMemberResponse> getChatRoomMembers(Long groupId, Long roomId, Long userId) {
        return chatRoomQueryService.getChatRoomMembers(groupId, roomId, userId);
    }

    /**
     * 특정 채팅방의 상세 정보를 조회합니다.
     *
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     * @param userId 요청자 ID
     * @return 채팅방 상세 정보
     */
    public ChatRoomDetailResponse getChatRoomDetail(Long groupId, Long roomId, Long userId) {
        return chatRoomQueryService.getChatRoomDetail(groupId, roomId, userId);
    }

    /**
     * 채팅방에 새 참여자를 초대합니다.
     *
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     * @param userId 요청자 ID
     * @param req 초대할 사용자 정보
     */
    @Transactional
    public void addParticipant(
            Long groupId,
            Long roomId,
            Long userId,
            AddParticipantRequest req
    ) {
        chatRoomCommandService.addParticipant(groupId, roomId, userId, req);
    }

    /**
     * 채팅방에서 특정 참여자를 강퇴합니다.
     *
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     * @param userId 요청자 ID
     * @param targetUserId 강퇴 대상 사용자 ID
     */
    @Transactional
    public void removeParticipant(
            Long groupId,
            Long roomId,
            Long userId,
            Long targetUserId
    ) {
        chatRoomCommandService.removeParticipant(groupId, roomId, userId, targetUserId);
    }

    /**
     * 현재 사용자가 해당 채팅방에서 나갑니다.
     *
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void leaveChatRoom(
            Long groupId,
            Long roomId,
            Long userId
    ) {
        chatRoomCommandService.leaveChatRoom(groupId, roomId, userId);
    }

    /**
     * 해당 채팅방의 사용자별 읽음 상태를 조회합니다.
     *
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     * @param userId 요청자 ID
     * @return 본인의 마지막 읽은 메시지 ID, 읽지 않은 메시지 수, 다른 참가자의 읽음 상태
     */
    public ReadStatusRestResponse getReadStatus(Long groupId, Long roomId, Long userId) {
        return chatRoomQueryService.getReadStatus(groupId, roomId, userId);
    }
}
