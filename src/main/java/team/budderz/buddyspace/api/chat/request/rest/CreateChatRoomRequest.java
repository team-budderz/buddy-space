package team.budderz.buddyspace.api.chat.request.rest;

import team.budderz.buddyspace.infra.database.chat.entity.ChatRoomType;

import java.util.List;

/**
 * 채팅방 생성 요청 DTO
 */
public record CreateChatRoomRequest(
        String name,              // 채팅방 이름
        String description,
        ChatRoomType chatRoomType,  // GROUP or DIRECT
        List<Long> participantIds   // 참여자 유저 ID 목록
) {}
