package team.budderz.buddyspace.global.oauth2.handler;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        log.info("OAuth2 로그인 성공 처리 시작");

        DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String accessToken = (String) attributes.get("accessToken");
        String refreshToken = (String) attributes.get("refreshToken");

        log.info("AccessToken: {}, RefreshToken: {}", accessToken != null, refreshToken != null);

        if (accessToken == null || refreshToken == null) {
            log.error("토큰이 null입니다");
            response.sendRedirect("http://localhost:3000/auth/callback?error=token_not_found&message=" +
                java.net.URLEncoder.encode("토큰 생성에 실패했습니다.", java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        // RefreshToken을 HttpOnly 쿠키로 설정
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofDays(14))
            .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.info("AccessToken 추출 성공: {}", accessToken.substring(0, Math.min(20, accessToken.length())) + "...");

        String redirectUrl = "https://budderz.co.kr/auth/callback?token=" + accessToken + "&success=true";
        log.info("리다이렉트 URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}