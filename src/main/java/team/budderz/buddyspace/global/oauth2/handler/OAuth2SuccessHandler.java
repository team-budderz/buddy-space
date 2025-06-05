package team.budderz.buddyspace.global.oauth2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import team.budderz.buddyspace.api.user.response.LoginResponse;
import team.budderz.buddyspace.domain.user.exception.UserErrorCode;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.domain.user.service.UserService;
import team.budderz.buddyspace.global.security.JwtUtil;
import team.budderz.buddyspace.global.util.RedisUtil;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;
    private final UserService userService;
    private final UserRepository userRepository;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.INVALID_USER_EMAIL));

        LoginResponse tokens = userService.login(user);

        addTokenCookies(response, tokens);

        response.sendRedirect(redirectUri);
    }

    private void addTokenCookies(HttpServletResponse response, LoginResponse tokens) {
        Cookie accessCookie = createCookie("accessToken", tokens.accessToken(), 1800); // 30분
        Cookie refreshCookie = createCookie("refreshToken", tokens.refreshToken(), 604800); // 7일

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true); // JS 접근 금지
        cookie.setSecure(true);   // HTTPS에서만 전송
        cookie.setMaxAge(maxAge);

        return cookie;
    }
}