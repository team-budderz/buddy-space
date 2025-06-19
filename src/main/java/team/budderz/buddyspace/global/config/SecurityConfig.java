package team.budderz.buddyspace.global.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import team.budderz.buddyspace.global.oauth2.handler.OAuth2SuccessHandler;
import team.budderz.buddyspace.global.oauth2.service.CustomOAuth2UserService;
import team.budderz.buddyspace.global.security.SecurityFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource; // 배포 후 설정

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/favicon.ico");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 운영할 땐 disable xx
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/favicon.ico",
                                "/test-websocket.html",
                                "/ws/**",
                                "/static/**",
                                "/api/users/login",
                                "/api/users/signup",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/healthy-check",
                                "/api/token/refresh"
                        ).permitAll()
                        .anyRequest().authenticated()
                        )
                // HTML 페이지가 내려오는 이유는 SecurityFilter 가 토큰을 넣어줘도 인증 실패시 로그인 페이지로 이동하게 되어 있기 때문
                .exceptionHandling(ex -> ex
                        // 인증이 되지 않은 사용자가 요청할 경우 401 응답
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\": \"인증이 필요합니다.\"}");
                        })
                        // 인증은 되었지만 권한이 부족한 경우 403 응답
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\": \"접근 권한이 없습니다.\"}");
                        })
                )
                .oauth2Login(oauth ->
                        oauth.userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService))
                                .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable());

                return http.build();
    }
}
