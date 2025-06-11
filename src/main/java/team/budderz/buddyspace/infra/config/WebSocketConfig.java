package team.budderz.buddyspace.infra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import team.budderz.buddyspace.global.security.JwtHandshakeInterceptor;
import team.budderz.buddyspace.global.security.JwtUtil;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    /* 클라이언트가 최초로 WebSocket 연결을 시도할 때 접속할 엔드포인트 설정 */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 	서버 내부 엔드포인트 경로 설정 + CORS 설정 + SockJS 프로토콜 지원 추가 (fallback)
        registry.addEndpoint("/ws")
//                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil)) // 핸드셰이크 단계에서 JWT 토큰을 검증해서 인증된 사용자만 WebSocket 연결을 허용
                .setAllowedOrigins(
                        "http://localhost:8080",             // 로컬테스트
                        "https://app.mydomain.com"         // 추후 운영서버
                )
                .withSockJS();
    }

    /* STOMP 메시지 라우팅 규칙 설정 */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트 --> 서버 prefix
        registry.setApplicationDestinationPrefixes("/pub");

        // 서버 --> 클라이언트 prefix
        registry.enableSimpleBroker("/sub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        if (jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
                            accessor.getSessionAttributes()
                                    .put("userId", jwtUtil.getUserIdFromToken(token));
                            return message;   // 인증 성공
                        }
                    }
                    throw new IllegalArgumentException("Invalid or missing token");
                }
                return message;
            }
        });
    }

}
