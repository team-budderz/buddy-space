package team.budderz.buddyspace.api.membership.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.budderz.buddyspace.api.membership.response.MembershipResponse;
import team.budderz.buddyspace.domain.membership.service.MembershipService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
@Tag(name = "모임 초대 관리", description = "모임 초대 관련 API")
public class MembershipInviteController {

    private final MembershipService membershipService;

    @Operation(
            summary = "초대 링크로 모임 가입",
            description = "로그인한 사용자가 초대 링크를 통해 특정 모임에 가입합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "초대 링크로 모임 가입 성공")
            }
    )
    @PostMapping
    public BaseResponse<MembershipResponse> inviteJoin(@RequestParam String code,
                                                       @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        MembershipResponse response = membershipService.inviteJoin(loginUserId, code);

        return new BaseResponse<>(response);
    }
}
