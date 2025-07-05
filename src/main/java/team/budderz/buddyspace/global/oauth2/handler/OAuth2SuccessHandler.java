package team.budderz.buddyspace.global.oauth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import team.budderz.buddyspace.api.auth.response.TokenResponse;
import team.budderz.buddyspace.global.oauth2.service.CustomOAuth2UserService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("OAuth2 로그인 성공 처리 시작");

        TokenResponse tokenResponse = (TokenResponse) RequestContextHolder.getRequestAttributes()
                .getAttribute(CustomOAuth2UserService.LOGIN_RESPONSE_ATTR, RequestAttributes.SCOPE_REQUEST);

        String refreshToken = (String) RequestContextHolder.getRequestAttributes()
                .getAttribute("REFRESH_TOKEN", RequestAttributes.SCOPE_REQUEST);

        log.info("TokenResponse: {}, RefreshToken: {}", tokenResponse != null, refreshToken != null);

        if (tokenResponse == null || refreshToken == null) {
            log.error("토큰이 null입니다");
            response.sendRedirect("http://localhost:3000/auth/callback?error=token_not_found&message=" +
                    java.net.URLEncoder.encode("토큰 생성에 실패했습니다.", java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        // RefreshToken을 HttpOnly 쿠키로 설정
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // TokenResponse에서 accessToken 추출
        String accessToken = extractAccessToken(tokenResponse);

        if (accessToken == null) {
            log.error("AccessToken 추출 실패");
            response.sendRedirect("http://localhost:3000/auth/callback?error=token_extraction_failed&message=" +
                    java.net.URLEncoder.encode("토큰 추출에 실패했습니다.", java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        log.info("AccessToken 추출 성공: {}", accessToken.substring(0, Math.min(20, accessToken.length())) + "...");

        // 프론트엔드로 리다이렉트
        String redirectUrl = "http://localhost:3000/auth/callback?token=" + accessToken + "&success=true";
        log.info("리다이렉트 URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private String extractAccessToken(TokenResponse tokenResponse) {
        try {
            // TokenResponse 클래스 정보 로깅
            Class<?> clazz = tokenResponse.getClass();
            log.info("TokenResponse 클래스: {}", clazz.getName());

            // 모든 필드 출력
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(tokenResponse);
                    log.info("필드 {}: {}", field.getName(), value);
                } catch (Exception e) {
                    log.warn("필드 {} 접근 실패", field.getName());
                }
            }

            // 모든 메서드 출력
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
                    log.info("Getter 메서드: {}", method.getName());
                }
            }

            // 1. getAccessToken() 시도
            try {
                Method getAccessToken = clazz.getMethod("getAccessToken");
                return (String) getAccessToken.invoke(tokenResponse);
            } catch (Exception e) {
                log.debug("getAccessToken() 메서드 없음");
            }

            // 2. getToken() 시도
            try {
                Method getToken = clazz.getMethod("getToken");
                return (String) getToken.invoke(tokenResponse);
            } catch (Exception e) {
                log.debug("getToken() 메서드 없음");
            }

            // 3. accessToken 필드 시도
            try {
                Field accessTokenField = clazz.getDeclaredField("accessToken");
                accessTokenField.setAccessible(true);
                return (String) accessTokenField.get(tokenResponse);
            } catch (Exception e) {
                log.debug("accessToken 필드 없음");
            }

            // 4. token 필드 시도
            try {
                Field tokenField = clazz.getDeclaredField("token");
                tokenField.setAccessible(true);
                return (String) tokenField.get(tokenResponse);
            } catch (Exception e) {
                log.debug("token 필드 없음");
            }

            log.error("AccessToken을 추출할 수 있는 방법을 찾지 못했습니다");
            return null;

        } catch (Exception e) {
            log.error("AccessToken 추출 중 오류 발생", e);
            return null;
        }
    }
}
