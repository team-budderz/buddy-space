package team.budderz.buddyspace.global.oauth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import team.budderz.buddyspace.api.auth.response.TokenResponse;
import team.budderz.buddyspace.api.user.response.LoginResponse;
import team.budderz.buddyspace.global.oauth2.service.CustomOAuth2UserService;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        TokenResponse tokenResponse = (TokenResponse) RequestContextHolder.getRequestAttributes()
                .getAttribute(CustomOAuth2UserService.LOGIN_RESPONSE_ATTR, RequestAttributes.SCOPE_REQUEST);

        String refreshToken = (String) RequestContextHolder.getRequestAttributes()
                .getAttribute("REFRESH_TOKEN", RequestAttributes.SCOPE_REQUEST);

        if (tokenResponse == null || refreshToken == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Token not found");
            return;
        }

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = objectMapper.writeValueAsString(tokenResponse);
        response.getWriter().write(json);
    }
}