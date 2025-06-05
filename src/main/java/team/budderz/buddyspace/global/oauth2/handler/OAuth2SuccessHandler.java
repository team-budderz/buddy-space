package team.budderz.buddyspace.global.oauth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import team.budderz.buddyspace.api.user.response.LoginResponse;
import team.budderz.buddyspace.global.oauth2.service.CustomOAuth2UserService;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // CustomOAuth2UserService 에서 저장해둔 loginResponse 꺼냄
        LoginResponse tokens = (LoginResponse) RequestContextHolder.getRequestAttributes()
                .getAttribute(CustomOAuth2UserService.LOGIN_RESPONSE_ATTR, RequestAttributes.SCOPE_REQUEST);

        if (tokens == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("로그인 처리에 실패했습니다.");
            return;
        }

        // JSON 응답 처리
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(tokens));
    }
}