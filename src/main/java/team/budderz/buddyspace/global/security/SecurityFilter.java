package team.budderz.buddyspace.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserDetailsService userDetailsService;

    /**
     * JWT 기반 인증을 처리하는 Spring Security 필터로, 유효한 Access 토큰이 있는 경우 인증 정보를 설정합니다.
     *
     * HTTP 요청에서 JWT 토큰을 추출하여 블랙리스트 여부와 유효성을 검사하고, Access 토큰이 아닌 경우 또는 유효하지 않은 경우에는 JSON 형식의 에러 응답을 반환합니다. 인증이 성공하면 SecurityContext에 인증 정보를 저장하고, 그렇지 않으면 적절한 에러 코드와 메시지로 응답을 종료합니다. 토큰이 없는 경우 또는 인증이 필요 없는 경로에서는 필터 체인을 계속 진행합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (requestURI.equals("/api/token/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtUtil.extractToken(request);
        // JWT 토큰이 HTTP 요청에 없을 경우, 쿠키에서 꺼내서 사용
        if (token == null) {
            token = extractTokenFromCookie(request, "accessToken");
        }

        if (token != null) {
            // 블랙리스트 확인
            if (redisTemplate.hasKey(token)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "AUTH_BLACKLISTED", "만료되었거나 로그아웃된 토큰입니다.");
                return; // 예외처리 하면 최상위 예외로 발생
            }

            try {
                // 토큰 검증 + ACCESS 토큰인지 확인
                if (jwtUtil.validateToken(token)) {
                    if (!jwtUtil.isAccessToken(token)) {
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                                "INVALID_TOKEN_TYPE", "Access 토큰이 아닙니다.");
                        return;
                    }

                    String email = jwtUtil.getEmailFromToken(token);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "INVALID_TOKEN", "토큰이 유효하지 않거나 만료되었습니다.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * JSON 형식의 에러 응답을 클라이언트에 전송합니다.
     *
     * @param response HTTP 응답 객체
     * @param status HTTP 상태 코드
     * @param code 에러 코드 문자열
     * @param message 에러 메시지
     * @throws IOException 응답 작성 중 입출력 오류가 발생할 경우
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"status\": %d, \"code\": \"%s\", \"message\": \"%s\"}",
                status, code, message
        ));
    }

    /**
     * [수정] 쿠키에서 토큰을 추출하는 메서드
     *
     * @param request    HTTP 요청
     * @param cookieName 찾을 쿠키 이름
     * @return 쿠키 값 또는 null
     */
    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;

        for (var cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
