package team.budderz.buddyspace.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import team.budderz.buddyspace.api.user.response.LoginResponse;
import team.budderz.buddyspace.global.util.RedisUtil;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    public LoginResponse createToken(User user) {
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        redisUtil.setData("RT:" + user.getId(), refreshToken, jwtUtil.getRefreshTokenExpireTime());

        return new LoginResponse(accessToken, refreshToken);
    }
}
