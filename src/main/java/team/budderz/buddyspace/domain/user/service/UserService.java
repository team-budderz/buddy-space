package team.budderz.buddyspace.domain.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team.budderz.buddyspace.api.user.request.*;
import team.budderz.buddyspace.api.user.response.LoginResponse;
import team.budderz.buddyspace.api.user.response.SignupResponse;
import team.budderz.buddyspace.api.user.response.UserUpdateResponse;
import team.budderz.buddyspace.domain.user.exception.UserErrorCode;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.global.exception.GlobalExceptionHandler;
import team.budderz.buddyspace.global.security.JwtUtil;
import team.budderz.buddyspace.global.security.UserAuth;
import team.budderz.buddyspace.global.util.RedisUtil;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.repository.ChatMessageRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;
import team.budderz.buddyspace.infra.database.comment.repository.CommentRepository;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;
import team.budderz.buddyspace.infra.database.mission.repository.MissionPostRepository;
import team.budderz.buddyspace.infra.database.mission.repository.MissionRepository;
import team.budderz.buddyspace.infra.database.post.repository.PostRepository;
import team.budderz.buddyspace.infra.database.schedule.repository.ScheduleRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;
import team.budderz.buddyspace.infra.database.vote.entity.VoteSelection;
import team.budderz.buddyspace.infra.database.vote.repository.VoteRepository;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final MembershipRepository membershipRepository;
    private final RedisUtil redisUtil;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        if(userRepository.findByEmail(signupRequest.email()).isPresent()) {
            throw new UserException(UserErrorCode.INVALID_USER_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.password());

        User user = User.builder()
                .name(signupRequest.name())
                .email(signupRequest.email())
                .password(encodedPassword)
                .birthDate(signupRequest.birthDate())
                .gender(signupRequest.gender())
                .address(signupRequest.address())
                .phone(signupRequest.phone())
                .provider(signupRequest.provider())
                .role(signupRequest.role())
                .build();

        userRepository.save(user);

        return SignupResponse.from(user);
    }

    public LoginResponse login(User user) {
        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.INVALID_USER_EMAIL);
        }

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        redisUtil.setData("RT:" + user.getId(), refreshToken, jwtUtil.getRefreshTokenExpireTime());

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(
                () -> new UserException(UserErrorCode.INVALID_USER_EMAIL)
        );

        if(!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new UserException(UserErrorCode.INVALID_USER_PASSWORD);
        }

        return login(user);
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

        // refresh token 삭제
        Long userId = jwtUtil.getUserIdFromToken(token);
        redisUtil.deleteData("RT:" + userId);
    }

    @Transactional
    public UserUpdateResponse updateUser(UserAuth userAuth, UserUpdateRequest updateRequest) {
        User user = userRepository.findById(userAuth.getUserId()).orElseThrow(
                () -> new UserException(UserErrorCode.INVALID_USER_ID)
        );

        if(!passwordEncoder.matches(updateRequest.password(), user.getPassword())) {
            throw new UserException(UserErrorCode.INVALID_USER_PASSWORD);
        }

       user.updateUser(updateRequest.address(), updateRequest.phone(), updateRequest.imageUrl());

       return UserUpdateResponse.from(user);
    }

    @Transactional
    public void updateUserPassword(UserAuth userAuth, UserPasswordUpdateRequest updateRequest) {
        User user = userRepository.findById(userAuth.getUserId()).orElseThrow(
                () -> new UserException(UserErrorCode.INVALID_USER_ID)
        );

        String encodedPassword = passwordEncoder.encode(updateRequest.password());

        user.updateUserPassword(encodedPassword);
    }

    @Transactional
    public void deleteUser(UserAuth userAuth, UserDeleteRequest deleteRequest) {
        User user = userRepository.findById(userAuth.getUserId()).orElseThrow(
                () -> new UserException(UserErrorCode.INVALID_USER_ID)
        );

        if(!passwordEncoder.matches(deleteRequest.password(), user.getPassword())) {
            throw new UserException(UserErrorCode.INVALID_USER_PASSWORD);
        }

        user.softDelete();

        membershipRepository.deleteAllByUser_Id(userAuth.getUserId());
    }

    //소셜로그인 테스트
    public SignupResponse getMyPage(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserException(UserErrorCode.INVALID_USER_ID)
        );
        return SignupResponse.from(user);
    }

}
