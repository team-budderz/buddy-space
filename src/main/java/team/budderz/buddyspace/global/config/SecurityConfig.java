package team.budderz.buddyspace.global.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
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

    /**
     * Spring Security의 주요 보안 필터 체인을 구성합니다.
     *
     * CSRF를 비활성화하고, CORS 설정을 적용하며, 세션을 무상태(Stateless)로 관리합니다. 회원가입, 로그인, OAuth2, 토큰 갱신, 헬스 체크, 정적 리소스, 테스트, Swagger 관련 엔드포인트는 인증 없이 접근을 허용하고, 그 외 모든 요청은 인증을 요구합니다.
     * 인증되지 않은 요청 시 Authorization 헤더의 유무 및 형식에 따라 401 상태와 함께 적절한 JSON 에러 메시지를 반환하며, 권한이 부족한 경우 403 상태와 JSON 에러 메시지를 반환합니다.
     * OAuth2 로그인 시 사용자 정보 서비스와 성공 핸들러를 지정하고, 커스텀 보안 필터를 UsernamePasswordAuthenticationFilter 앞에 추가합니다. HTTP Basic 및 폼 로그인을 비활성화합니다.
     *
     * @return 구성된 SecurityFilterChain 인스턴스
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 운영할 땐 disable xx
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/signup",
                                "/api/users/login",
                                "/login/oauth2/**",
                                "/oauth2/**",
                                "/api/token/refresh",
                                "/api/healthy-check"
                        ).permitAll()
                        .requestMatchers( // test
                                "/favicon.ico",
                                "/test-websocket.html",
                                "/test/**",
                                "/js/**",
                                "/css/**",
                                "/ws/**",
                                "/static/**"
                        ).permitAll()
                        .requestMatchers( // swagger
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
                // HTML 페이지가 내려오는 이유는 SecurityFilter 가 토큰을 넣어줘도 인증 실패시 로그인 페이지로 이동하게 되어 있기 때문
                .exceptionHandling(ex -> ex
                        // 인증이 되지 않은 사용자가 요청할 경우 401 응답
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
                        // 인증은 되었지만 권한이 부족한 경우 403 응답
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
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }
}
