package team.budderz.buddyspace.api.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.chat.request.rest.AddParticipantRequest;
import team.budderz.buddyspace.domain.chat.service.ChatRoomServiceFacade;
import team.budderz.buddyspace.global.security.UserAuth;

/**
 * 채팅방 참여자 관련 HTTP API 컨트롤러입니다.
 * - 참여자 초대
 * - 참여자 강퇴
 * - 본인 나가기
 */
@Tag(name = "채팅 참여자 관리", description = "채팅방 참여자 초대, 강퇴, 나가기 API")
@RestController
@RequestMapping("/api/group/{groupId}/chat/rooms/{roomId}/participants")
@RequiredArgsConstructor
public class ChatParticipantRestController {

    private final ChatRoomServiceFacade chatRoomService;

    /**
     * 채팅방에 새로운 사용자를 초대합니다.
     *
     * @param userAuth 현재 로그인한 사용자 정보
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     * @param req 초대할 대상 정보
     */
    @Operation(
            summary = "참여자 초대",
            description = "채팅방에 다른 사용자를 초대합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "참여자 초대 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addParticipant(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId,
            @RequestBody AddParticipantRequest req
    ) {
        chatRoomService.addParticipant(groupId, roomId, userAuth.getUserId(), req);
    }

    /**
     * 채팅방에서 특정 참여자를 강퇴합니다.
     *
     * @param userAuth 현재 로그인한 사용자 정보
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     * @param targetUserId 강퇴할 대상 사용자 ID
     */
    @Operation(
            summary = "참여자 강퇴",
            description = "채팅방에서 특정 사용자를 강퇴합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "참여자 강퇴 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "대상 사용자를 찾을 수 없음")
            }
    )
    @DeleteMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kickParticipant(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId,
            @PathVariable Long targetUserId
    ) {
        chatRoomService.removeParticipant(groupId, roomId, userAuth.getUserId(), targetUserId);
    }

    /**
     * 현재 사용자가 채팅방에서 나갑니다.
     *
     * @param userAuth 현재 로그인한 사용자 정보
     * @param groupId 그룹 ID
     * @param roomId 채팅방 ID
     */
    @Operation(
            summary = "채팅방 나가기",
            description = "현재 사용자가 채팅방에서 나갑니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "나가기 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "채팅방 또는 사용자 정보를 찾을 수 없음")
            }
    )
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveRoom(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId
    ) {
        chatRoomService.leaveChatRoom(groupId, roomId, userAuth.getUserId());
    }
}
