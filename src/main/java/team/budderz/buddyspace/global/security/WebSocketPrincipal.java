package team.budderz.buddyspace.global.security;

import java.security.Principal;

/** WebSocket 세션에서 인증된 사용자를 나타내는 Principal */
public record WebSocketPrincipal(Long userId) implements Principal {
    @Override public String getName() { return String.valueOf(userId); }
}
