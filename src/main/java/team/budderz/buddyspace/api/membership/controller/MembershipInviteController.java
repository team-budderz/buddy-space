package team.budderz.buddyspace.api.membership.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.membership.response.MembershipResponse;
import team.budderz.buddyspace.domain.membership.service.MembershipService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class MembershipInviteController {

    private final MembershipService membershipService;

    @PostMapping
    public BaseResponse<MembershipResponse> inviteJoin(@RequestParam String code,
                                                       @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        MembershipResponse response = membershipService.inviteJoin(loginUserId, code);

        return new BaseResponse<>(response);
    }
}
