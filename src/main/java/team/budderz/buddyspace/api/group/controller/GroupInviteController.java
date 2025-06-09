package team.budderz.buddyspace.api.group.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.group.response.GroupInviteResponse;
import team.budderz.buddyspace.domain.group.service.GroupInviteService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/invites")
public class GroupInviteController {

    private final GroupInviteService groupInviteService;

    @PatchMapping
    public BaseResponse<GroupInviteResponse> updateInviteLink(@PathVariable Long groupId,
                                                              @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupInviteResponse response = groupInviteService.updateInviteLink(loginUserId, groupId);

        return new BaseResponse<>(response);
    }

    @GetMapping
    public BaseResponse<GroupInviteResponse> findInviteLink(@PathVariable Long groupId,
                                                            @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupInviteResponse response = groupInviteService.findInviteLink(loginUserId, groupId);

        return new BaseResponse<>(response);
    }

    @DeleteMapping
    public BaseResponse<GroupInviteResponse> deleteInviteLink(@PathVariable Long groupId,
                                                              @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupInviteResponse response = groupInviteService.deleteInviteLink(loginUserId, groupId);

        return new BaseResponse<>(response);
    }

}
