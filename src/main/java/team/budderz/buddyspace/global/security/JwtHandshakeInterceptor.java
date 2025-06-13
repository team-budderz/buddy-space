package team.budderz.buddyspace.global.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
/**
 * WebSocket 핸드셰이크 단계에서 JWT(ACCESS) 검증 + principal, userId를 sessionAttributes 에 심기
 */
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        // 1. WebSocket 연결 요청이 Servlet 기반 HTTP 요청인지 확인
        // 이유: HttpServletRequest 에서 헤더를 읽으려고 하기 때문 + Spring WebSocket 지원 구조 때문
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return refuse("서블릿 요청이 아님");
        }

        HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

        /* 2. 토큰 추출: 쿼리 → 없으면 Authorization 헤더 */
        String token = extractToken(request, httpServletRequest);
        if (token == null) return refuse("토큰 없음");

        /* 3. 검증 */
        if (!jwtUtil.validateToken(token))      return refuse("유효하지 않은 토큰");
        if (!jwtUtil.isAccessToken(token))      return refuse("ACCESS 토큰 아님");

        /* 4. 세션에 사용자 식별 정보 저장 */
        Long userId = jwtUtil.getUserIdFromToken(token);
        attributes.put("userId", userId);                          // 기존 코드 호환
        attributes.put("principal", new WebSocketPrincipal(userId)); // simpUser 헤더 주입용
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }

    /* ━━━━━━━━━ helper ━━━━━━━━━ */

    private String extractToken(ServerHttpRequest req, HttpServletRequest http) {
        // 1. 쿼리 파라미터 access_token
        String token = UriComponentsBuilder.fromUri(req.getURI())
                .build()
                .getQueryParams()
                .getFirst("access_token");
        if (token != null) return token;

        // 2. Authorization: Bearer ...
        String header = http.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private boolean refuse(String reason) {
        System.out.println("WebSocket 연결 거부: " + reason);
        return false;
    }

}
