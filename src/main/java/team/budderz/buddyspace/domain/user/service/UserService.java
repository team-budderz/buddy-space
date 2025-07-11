package team.budderz.buddyspace.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.budderz.buddyspace.api.auth.response.TokenResponse;
import team.budderz.buddyspace.api.user.request.*;
import team.budderz.buddyspace.api.user.response.SignupResponse;
import team.budderz.buddyspace.api.user.response.UserDetailResponse;
import team.budderz.buddyspace.api.user.response.UserUpdateResponse;
import team.budderz.buddyspace.domain.attachment.service.AttachmentService;
import team.budderz.buddyspace.domain.chat.service.ChatRoomCommandService;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.domain.user.exception.UserErrorCode;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.domain.user.provider.UserProfileImageProvider;
import team.budderz.buddyspace.global.security.JwtUtil;
import team.budderz.buddyspace.global.util.AddressNormalizer;
import team.budderz.buddyspace.global.util.RedisUtil;
import team.budderz.buddyspace.infra.client.s3.DefaultImageProvider;
import team.budderz.buddyspace.infra.database.attachment.entity.Attachment;
import team.budderz.buddyspace.infra.database.chat.repository.ChatMessageRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatRoomRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ReadHistoryRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.repository.GroupPermissionRepository;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.entity.UserProvider;
import team.budderz.buddyspace.infra.database.user.entity.UserRole;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final MembershipRepository membershipRepository;
    private final RedisUtil redisUtil;
    private final DefaultImageProvider defaultImageProvider;
    private final UserProfileImageProvider profileImageProvider;
    private final AttachmentService attachmentService;
    private final ChatRoomCommandService chatRoomCommandService;
    private final GroupValidator groupValidator;
    private final GroupRepository groupRepository;
    private final GroupPermissionRepository groupPermissionRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ReadHistoryRepository readHistoryRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        if (userRepository.findByEmail(signupRequest.email()).isPresent()) {
            throw new UserException(UserErrorCode.INVALID_USER_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.password());
        String normalizeAddress = AddressNormalizer.normalizeAddress(signupRequest.address()); // 주소 정제

        User user = User.builder()
                .name(signupRequest.name())
                .email(signupRequest.email())
                .password(encodedPassword)
                .birthDate(signupRequest.birthDate())
                .gender(signupRequest.gender())
                .address(normalizeAddress)
                .phone(signupRequest.phone())
                .provider(UserProvider.LOCAL)
                .role(UserRole.USER)
                .build();

        userRepository.save(user);

        return SignupResponse.from(user);
    }

    public TokenResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(
                () -> new UserException(UserErrorCode.INVALID_USER_EMAIL)
        );

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new UserException(UserErrorCode.INVALID_USER_PASSWORD);
        }

        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.INVALID_USER_EMAIL);
        }

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        // Redis 저장
        redisUtil.setData("RT:" + user.getId(), refreshToken, jwtUtil.getRefreshTokenExpireTime());

        // accessToken 쿠키 설정
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        // refreshToken 쿠키 설정
        ResponseCookie refreshTokenCookie  = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return new TokenResponse(accessToken);
    }

    public void logout(String token) {
        // 토큰 없을 경우
        if (token == null) {
            throw new UserException(UserErrorCode.INVALID_USER_REQUEST);
        }

        if (token.startsWith("Bearer")) {
            token = token.substring(7);
        }

        // 무결성 검증
        if (!jwtUtil.validateToken(token)) {
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
    public UserUpdateResponse updateUser(
            Long userId, String passwordToken, HttpServletResponse response,
            UserUpdateRequest updateRequest, MultipartFile profileImage
    ) {
        if (!attachmentService.isImage(profileImage)) {
            throw new UserException(UserErrorCode.INVALID_IMAGE_TYPE);
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserException(UserErrorCode.INVALID_USER_ID)
        );

        validatePasswordToken(userId, passwordToken, response);

        String normalizeAddress = AddressNormalizer.normalizeAddress(updateRequest.address()); // 주소 정제
        Neighborhood neighborhood = user.getNeighborhood(); // 사용자 동네 인증 정보

        if (user.getNeighborhood() != null) {
            // 기존에 인증한 동네 주소
            String verifiedAddress = user.getNeighborhood().getVerifiedAddress();

            // 기존 동네와 변경한 동네가 같은지 검증 - 같은 동네면 인증 유지, 다르면 인증 정보 제거
            if (!verifiedAddress.equals(normalizeAddress)) {
                neighborhood = null;
            }
        }

        user.updateUser(normalizeAddress, neighborhood, updateRequest.phone());

        Attachment profileAttachment = null;

        if (profileImage != null && !profileImage.isEmpty()) {
            // 기존 프로필 이미지가 기본 이미지가 아니면 삭제
            Attachment oldAttachment = user.getProfileAttachment();
            if (oldAttachment != null && !defaultImageProvider.isDefaultProfileKey(oldAttachment.getKey())) {
                attachmentService.delete(oldAttachment.getId());
            }
            // 새 프로필 이미지 업로드
            profileAttachment = profileImageProvider.getProfileAttachment(profileImage, userId);

        } else if (updateRequest.profileAttachmentId() != null) {
            // 기존 이미지 유지
            profileAttachment = attachmentService.findAttachmentOrThrow(updateRequest.profileAttachmentId());

        } else {
            // 기존 프로필 이미지가 기본 이미지가 아니면 삭제
            Attachment oldAttachment = user.getProfileAttachment();
            if (oldAttachment != null && !defaultImageProvider.isDefaultProfileKey(oldAttachment.getKey())) {
                attachmentService.delete(oldAttachment.getId());
            }
        }

        user.updateProfileAttachment(profileAttachment);
        String profileImageUrl = profileImageProvider.getProfileImageUrl(user);

        return UserUpdateResponse.from(user, profileImageUrl);
    }

    @Transactional
    public void updateUserPassword(Long userId, String passwordToken, HttpServletResponse response, UserPasswordUpdateRequest updateRequest) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserException(UserErrorCode.INVALID_USER_ID)
        );

        validatePasswordToken(userId, passwordToken, response);
        String encodedPassword = passwordEncoder.encode(updateRequest.password());

        user.updateUserPassword(encodedPassword);
    }

    @Transactional
    public void deleteUser(Long userId, String passwordToken, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        validatePasswordToken(userId, passwordToken, response);

        // 리더이면서 멤버가 존재하는 모임이 있는지 검증 (있으면 예외 발생)
        groupValidator.validateUserCanBeDeleted(userId);

        try {
            // 사용자가 생성한 모든 모임 조회
            List<Group> groups = groupRepository.findAllByLeader_Id(userId);

            for (Group group : groups) {
                // 해당 모임들의 권한 설정 제거
                groupPermissionRepository.deleteAllByGroup_Id(group.getId());

                // 해당 모임들에 가입 요청 중이거나 차단된 회원들과의 관계 제거
                membershipRepository.deleteAllByGroup_Id(group.getId());
            }

            // 사용자가 속한 모든 모임에서 탈퇴 또는 가입 요청 취소 처리
            membershipRepository.deleteAllByUser_Id(userId);

            // 사용자가 생성한 모든 모임 삭제
            groupRepository.deleteAllByLeader_Id(userId);

            // 사용자 프로필 이미지 삭제 (기본 이미지가 아닌 경우)
            Attachment profileAttachment = user.getProfileAttachment();
            if (profileAttachment != null && !defaultImageProvider.isDefaultProfileKey(profileAttachment.getKey())) {
                attachmentService.delete(profileAttachment.getId());
            }

            // 유저가 참여 중이었던 모든 채팅방 ID 목록 조회
            List<Long> roomIds = chatParticipantRepository.findActiveRoomIdsByUserId(userId);


            // 유저 제거 이후, 각 채팅방의 남은 참여자 수 확인
            for (Long roomId : roomIds) {
                // 각 채팅방의 groupId 조회 필요
                Long groupId = chatRoomRepository.findGroupIdByRoomId(roomId);
                chatRoomCommandService.leaveChatRoom(groupId, roomId, userId);
            }

            // 사용자 채팅 참가 정보 삭제
            chatParticipantRepository.deleteByUserId(userId);

            // 사용자 읽음 이력 삭제
            readHistoryRepository.deleteByUserId(userId);

            // 사용자 계정 삭제
            userRepository.deleteById(userId);

        } catch (Exception e) {
            log.error("사용자 삭제 중 오류 발생: userId={}", userId, e);
            throw new UserException(UserErrorCode.USER_DELETE_FAILED);
        }
    }

    public void verifyPassword(Long userId, PasswordRequest request, HttpServletResponse response) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserException(UserErrorCode.INVALID_USER_ID)
        );

        validatePassword(request.password(), user.getPassword());

        // 인증 토큰 발급 & Redis 저장
        String verificationToken = UUID.randomUUID().toString();
        redisUtil.setData("pw:" + user.getId(), verificationToken, 300_000);

        // 쿠키에 저장
        ResponseCookie cookie = ResponseCookie.from("verified_password", verificationToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(5))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void validatePasswordToken(Long userId, String passwordToken, HttpServletResponse response) {
        String redisToken = redisUtil.getData("pw:" + userId);
        if (redisToken == null || !redisToken.equals(passwordToken)) {
            throw new UserException(UserErrorCode.INVALID_USER_PASSWORD);
        }

        // Redis 토큰 소모
        redisUtil.deleteData("pw:" + userId);

        // 쿠키 제거
        Cookie cookie = new Cookie("verified_password", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new UserException(UserErrorCode.INVALID_USER_PASSWORD);
        }
    }

    public UserDetailResponse getMyPage(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserException(UserErrorCode.INVALID_USER_ID)
        );

        String profileImageUrl = profileImageProvider.getProfileImageUrl(user);

        return UserDetailResponse.from(user, profileImageUrl);
    }
}
