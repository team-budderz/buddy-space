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

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String token = jwtUtil.extractToken(request);

        if(token != null) {
            // 블랙리스트 확인
            if(Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            try {
                // 토큰 검증 + ACCESS 토큰인지 확인
                if(jwtUtil.validateToken(token)) {
                    if(!jwtUtil.isAccessToken(token)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }

                    // 인증 객체 설정
                    UserAuth userAuth = jwtUtil.extractUserAuth(token);

                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + userAuth.getRole().name())
                    );

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userAuth, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 접근입니다.");
            }
        }
        filterChain.doFilter(request, response);
    }
}
