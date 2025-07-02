package team.budderz.buddyspace.api.chat.response.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 참여자 응답 DTO")
public record ChatRoomMemberResponse(

        @Schema(description = "참여자 유저 ID", example = "101")
        Long userId,

        @Schema(description = "참여자 이름", example = "홍길동")
        String name,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/images/user.png")
        String profileUrl
) {}
