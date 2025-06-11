package team.budderz.buddyspace.global.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

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

            // JWT 토큰 추출 (Authorization 헤더에서 Bearer 토큰 가져오기)
            String token = jwtUtil.extractToken(httpServletRequest);

            // 토큰이 존재하고, 유효한지, 그리고 ACCESS 토큰인지 확인
            if (token != null && jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
                // 인증 성공 → 사용자 정보(UserAuth)를 WebSocket 세션에 저장
                Long userId = jwtUtil.getUserIdFromToken(token);
                attributes.put("userId", userId); // 이후 채팅 메시지 처리에서 사용
                return true; // → WebSocket 연결 허용
            } else {
                // 인증 실패 → WebSocket 연결 거부
                if (token == null) {
                    System.out.println("❌ WebSocket 연결 실패: 토큰 없음");
                    return false;
                }
                if (!jwtUtil.validateToken(token)) {
                    System.out.println("❌ WebSocket 연결 실패: 토큰 유효하지 않음");
                    return false;
                }
                if (!jwtUtil.isAccessToken(token)) {
                    System.out.println("❌ WebSocket 연결 실패: 액세스 토큰 아님");
                    return false;
                }

            }
        }
        // Servlet 기반 요청이 아니면 거부
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 아무것도 안 해도 됨
        // afterHandshake 는 지금은 별도 로직 필요 없음 (빈 메서드)
    }

}
