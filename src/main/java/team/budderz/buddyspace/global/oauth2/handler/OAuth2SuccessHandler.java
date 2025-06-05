package team.budderz.buddyspace.global.oauth2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import team.budderz.buddyspace.domain.user.exception.UserErrorCode;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.global.security.JwtUtil;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.INVALID_USER_ID));

        String accessToken = jwtUtil.createAccessToken(user.getId(), email, user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        response.sendRedirect("http://localhost:3000/oauth/success?accessToken=" + accessToken);

    }
}
