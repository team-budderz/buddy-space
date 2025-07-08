package team.budderz.buddyspace.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/favicon.ico");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                    "/api/users/signup",
                    "/api/users/login",
                    "/login/oauth2/**",  // 기존 OAuth2 엔드포인트 유지
                    "/oauth2/**",
                    "/api/token/refresh",
                    "/api/healthy-check",
                    "/api/users/me",
                    "/login/oauth2/code/**"
                ).permitAll()
                .requestMatchers(
                    "/favicon.ico",
                    "/test/**",
                    "/js/**",
                    "/css/**",
                    "/ws",
                    "/ws/**",
                    "/pub/**",
                    "/sub/**"
                ).permitAll()
                .requestMatchers(
                    "/api-ui",
                    "/api-ui/**",
                    "/swagger-ui",
                    "/swagger-ui/**",
                    "/api-docs",
                    "/api-docs/**",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String token = request.getHeader("Authorization");
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                    if (token == null || !token.startsWith("Bearer ")) {
                        response.getWriter().write("{\"status\": 401, \"code\": \"NO_TOKEN\", \"message\": \"인증 토큰이 없습니다. 로그인이 필요합니다.\"}");
                    } else {
                        response.getWriter().write("{\"status\": 401, \"code\": \"INVALID_CREDENTIAL\", \"message\": \"인증 정보가 유효하지 않습니다.\"}");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\": 403, \"code\": \"ACCESS_DENIED\", \"message\": \"접근 권한이 없습니다.\"}");
                })
            )
            .oauth2Login(oauth ->
                oauth.userInfoEndpoint(userInfo ->
                        userInfo.userService(customOAuth2UserService))
                    .successHandler(oAuth2SuccessHandler)
                    // 실패 시에도 프론트엔드로 리다이렉트
                    .failureHandler((request, response, exception) -> {
                        response.sendRedirect("https://budderz.co.kr/auth/callback?error=oauth_failed&message=" +
                            java.net.URLEncoder.encode("OAuth2 인증에 실패했습니다.", java.nio.charset.StandardCharsets.UTF_8));
                    })
            )
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }
}