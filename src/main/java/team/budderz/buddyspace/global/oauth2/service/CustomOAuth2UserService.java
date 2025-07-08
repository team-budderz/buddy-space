package team.budderz.buddyspace.global.oauth2.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.global.security.JwtUtil;
import team.budderz.buddyspace.global.util.RedisUtil;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.entity.UserGender;
import team.budderz.buddyspace.infra.database.user.entity.UserProvider;
import team.budderz.buddyspace.infra.database.user.entity.UserRole;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private static final String DEFAULT_ADDRESS = "주소 미입력";
    private static final String DEFAULT_PHONE = "010-1111-2222";

    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");
        String provider = userRequest.getClientRegistration().getRegistrationId(); // google
        UserProvider userProvider = UserProvider.valueOf(provider.toUpperCase()); // GOOGLE

        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User user = userRepository.findByEmailAndProvider(email, userProvider)
            .orElseGet(() -> {
                User newUser = new User(
                    name,
                    email,
                    randomPassword,
                    LocalDate.of(2000,1,1),
                    UserGender.UNKNOWN,
                    DEFAULT_ADDRESS,
                    DEFAULT_PHONE,
                    userProvider,
                    UserRole.USER
                );
                newUser.setProviderId(providerId);
                return userRepository.save(newUser);
            });

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        redisUtil.setData("RT:" + user.getId(), refreshToken, jwtUtil.getRefreshTokenExpireTime());

        attributes.put("accessToken", accessToken);
        attributes.put("refreshToken", refreshToken);

        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "sub"
        );
    }
}
