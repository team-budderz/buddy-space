package team.budderz.buddyspace.global.oauth2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.entity.UserGender;
import team.budderz.buddyspace.infra.database.user.entity.UserProvider;
import team.budderz.buddyspace.infra.database.user.entity.UserRole;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");
        String provider = userRequest.getClientRegistration().getRegistrationId(); // google
        UserProvider userProvider = UserProvider.valueOf(provider.toUpperCase()); // GOOGLE

        userRepository.findByEmailAndProvider(email, userProvider)
                .orElseGet(() -> {
                    User newUser = new User(
                            name,
                            email,
                            UUID.randomUUID().toString(),
                            LocalDate.of(2000,1,1),
                            UserGender.F,
                            "소셜 로그인 - 주소 미입력",
                            "010-1111-2222",
                            userProvider,
                            UserRole.USER
                    );
                    newUser.setProviderId(providerId);
                    return userRepository.save(newUser);
                });
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );
    }
}
