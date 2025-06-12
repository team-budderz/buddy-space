package team.budderz.buddyspace.global.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

// WebSocket 연결(핸드셰이크) 요청이 들어올 때 JWT 토큰을 검증해서 인증된 사용자만 WebSocket 연결 허용
// 인증된 사용자의 정보를 WebSocket 세션에 저장 → 이후 채팅 메시지 처리 시 활용 가능
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // WebSocket 연결 요청이 Servlet 기반 HTTP 요청인지 확인
        // 이유: HttpServletRequest 에서 헤더를 읽으려고 하기 때문 + Spring WebSocket 지원 구조 때문
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // 원본 HttpServletRequest 꺼내오기
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

            // 쿼리 파라미터에서 access_token 추출
            String token = UriComponentsBuilder.fromUri(request.getURI())
                    .build()
                    .getQueryParams()
                    .getFirst("access_token");

            // 토큰이 존재하고, 유효한지, 그리고 ACCESS 토큰인지 확인
            if (token != null && jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                attributes.put("userId", userId);
                return true;
            }
        }
        // Servlet 기반 요청이 아니면 거부
        System.out.println("WebSocket 연결 실패: 유효하지 않은 토큰");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 아무것도 안 해도 됨
        // afterHandshake 는 지금은 별도 로직 필요 없음 (빈 메서드)
    }

}
