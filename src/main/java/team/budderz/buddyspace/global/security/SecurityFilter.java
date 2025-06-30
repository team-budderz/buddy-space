package team.budderz.buddyspace.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserDetailsService userDetailsService;

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

        if(token != null) {
            // 블랙리스트 확인
            if(redisTemplate.hasKey(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return; // 예외처리 하면 최상위 예외로 발생
            }

            try {
                // 토큰 검증 + ACCESS 토큰인지 확인
                if(jwtUtil.validateToken(token)) {
                    if(!jwtUtil.isAccessToken(token)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }

                    String email = jwtUtil.getEmailFromToken(token);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 접근입니다.");
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * [수정] 쿠키에서 토큰을 추출하는 메서드
     * @param request HTTP 요청
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
