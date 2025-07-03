package team.budderz.buddyspace.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.budderz.buddyspace.api.auth.response.TokenResponse;
import team.budderz.buddyspace.domain.auth.service.AuthService;
import team.budderz.buddyspace.global.response.BaseResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
@Tag(name = "사용자 인증 관리", description = "사용자 인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "토큰 재발급",
            description = "리프레시 토큰으로 새로운 액세스 토큰을 재발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공")
            }
    )
    @PostMapping("/refresh")
    public BaseResponse<TokenResponse> reissueToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = authService.reissueToken(refreshToken, response);
        return new BaseResponse<>(tokenResponse);
    }
}
