package team.budderz.buddyspace.api.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.budderz.buddyspace.api.auth.response.TokenResponse;
import team.budderz.buddyspace.domain.auth.service.AuthService;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.JwtUtil;
import team.budderz.buddyspace.global.util.RedisUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class AuthController {

    private final AuthService authService;

    public BaseResponse<TokenResponse> reissueToken(
            @RequestHeader("Authorization") String refreshTokenHeader
    ) {
        // 접두어 제거
        String refreshToken = refreshTokenHeader.startsWith("Bearer ")
                ? refreshTokenHeader.substring(7)
                : refreshTokenHeader;

        return new BaseResponse<>(authService.reissueToken(refreshToken));

    }
}
