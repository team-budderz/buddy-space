package team.budderz.buddyspace.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

// @Configuration
// @Profile("deploy")
// public class CorsConfigDeploy implements WebMvcConfigurer {
    // public static final String ALLOWED_METHOD_NAMES = "GET,HEAD,POST,PUT,DELETE,TRACE,OPTIONS,PATCH";
    //
    // @Override
    // public void addCorsMappings(final CorsRegistry registry) {
    //     registry.addMapping("/**")
    //             .allowedOriginPatterns("*")
    //             .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
    //             .allowedHeaders("Authorization", "Content-Type", "Accept")
    //             .exposedHeaders("Authorization", "Set-Cookie")
    //             .allowCredentials(true)
    //             .maxAge(3600);
    // }


    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //     CorsConfiguration config = new CorsConfiguration();
    //     config.setAllowedOrigins(List.of("https://budderz.co.kr", "https://buddy-space-front-hwj4.vercel.app"));
    //     config.setAllowedMethods(List.of("GET","POST","PUT","DELETE", "PATCH", "OPTIONS"));
    //     config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    //     config.setAllowCredentials(true);
    //     config.setMaxAge(3600L); // 1시간 캐싱
    //
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", config);
    //     return source;
    // }
// }
