package team.budderz.buddyspace.domain.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team.budderz.buddyspace.api.user.request.LoginRequest;
import team.budderz.buddyspace.api.user.request.SignupRequest;
import team.budderz.buddyspace.api.user.response.LoginResponse;
import team.budderz.buddyspace.api.user.response.SignupResponse;
import team.budderz.buddyspace.domain.user.exception.UserErrorCode;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.global.exception.GlobalExceptionHandler;
import team.budderz.buddyspace.global.security.JwtUtil;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        if(userRepository.findByEmail(signupRequest.email()).isPresent()) {
            throw new UserException(UserErrorCode.INVALID_USER_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.password());

        User user = new User(
                signupRequest.name(),
                signupRequest.email(),
                encodedPassword,
                signupRequest.birthDate(),
                signupRequest.gender(),
                signupRequest.address(),
                signupRequest.phone(),
                signupRequest.provider(),
                signupRequest.role()
        );

        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(
                () -> new UserException(UserErrorCode.INVALID_USER_EMAIL)
        );

        // 회원 탈퇴 여부 확인 로직 필요

        if(!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new UserException(UserErrorCode.INVALID_USER_PASSWORD);
        }

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        return new LoginResponse(accessToken, refreshToken);
    }

    public void logout(String token) {
        // 토큰 없을 경우
        if(token == null) {
            throw new UserException(UserErrorCode.INVALID_USER_REQUEST);
        }

        if(token.startsWith("Bearer")) {
            token = token.substring(7);
        }

        // 무결성 검증
        if(!jwtUtil.validateToken(token)) {
            throw new UserException(UserErrorCode.INVALID_USER_REQUEST);
        }

        // redis 블랙리스트 등록시, TTL 설정을 위한 토큰 시간 계산
        Date expiration = jwtUtil.getExpiration(token);
        long now = System.currentTimeMillis();
        long expireIn = expiration.getTime() - now;

        // 데이터 충돌 방지를 위한 prefix 설정
        redisTemplate.opsForValue().set("blacklist:" + token, "logout", expireIn, TimeUnit.MILLISECONDS);
    }


}
