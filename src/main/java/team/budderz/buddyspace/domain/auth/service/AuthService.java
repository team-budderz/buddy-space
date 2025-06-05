package team.budderz.buddyspace.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.budderz.buddyspace.api.auth.response.TokenResponse;
import team.budderz.buddyspace.domain.auth.exception.AuthErrorCode;
import team.budderz.buddyspace.domain.auth.exception.AuthException;
import team.budderz.buddyspace.domain.user.exception.UserErrorCode;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.global.security.JwtUtil;
import team.budderz.buddyspace.global.util.RedisUtil;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final UserRepository userRepository;

    public TokenResponse reissueToken(String refreshToken) {

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String storedToken = redisUtil.getData("RT:" + userId);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.TOKEN_MISMATCH);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String newRefreshToken = jwtUtil.createRefreshToken(user.getId());

        redisUtil.setData("RT:" + userId, newRefreshToken, jwtUtil.getRefreshTokenExpireTime());

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

}
