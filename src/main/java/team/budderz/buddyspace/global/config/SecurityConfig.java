package team.budderz.buddyspace.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
    //private final CorsConfigurationSource corsConfigurationSource; // 배포 후 설정

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 운영할 땐 disable xx
                //.cors(cors -> cors.configurationSource(corsConfigurationSource)) // 배포 후 설정
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        //.requestMatchers("**").permitAll()
                        .requestMatchers(
                                "/api/users/login",
                                "/api/users/signup",
                                "/oauth2/**",
                                "/login/oauth2/**"
                                ).permitAll()
                        //.requestMatchers("**").hasRole("ADMIN") // 테스트용
                        .anyRequest().authenticated()
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
