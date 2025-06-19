package team.budderz.buddyspace.api.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/refresh")
    public BaseResponse<TokenResponse> reissueToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        return new BaseResponse<>(authService.reissueToken(refreshToken, response));
    }
}
