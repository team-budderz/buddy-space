package team.budderz.buddyspace.api.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.chat.request.rest.AddParticipantRequest;
import team.budderz.buddyspace.domain.chat.service.ChatRoomServiceFacade;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequestMapping("/api/group/{groupId}/chat/rooms/{roomId}/participants")
@RequiredArgsConstructor
public class ChatParticipantRestController {

    private final ChatRoomServiceFacade chatRoomService;

    /** 참여자 초대 */
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

    /** 참여자 강퇴 */
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

    /** 본인 채팅방 나가기 */
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
