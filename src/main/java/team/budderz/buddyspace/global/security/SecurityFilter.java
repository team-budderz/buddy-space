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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // 리프레시 토큰 재발급 경로는 이 필터를 건너뜀
        if (path.equals("/api/token/refresh")) {
            return true;
        }
        // OAuth2 인증 시작 및 콜백 경로는 JWT 토큰 검사가 필요 없으므로 건너뜀
        // Spring Security의 OAuth2 인증 필터가 이 경로들을 처리
        if (path.startsWith("/login/oauth2/authorization/") ||
            path.startsWith("/login/oauth2/code/")) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
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