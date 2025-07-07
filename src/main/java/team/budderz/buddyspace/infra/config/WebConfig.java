package team.budderz.buddyspace.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	public static final String ALLOWED_METHOD_NAMES = "GET,HEAD,POST,PUT,DELETE,TRACE,OPTIONS,PATCH";

	@Override
	public void addCorsMappings(final CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns("https://budderz.co.kr")
			.allowedMethods(ALLOWED_METHOD_NAMES.split(","))
			.exposedHeaders("Authorization", "Member", "Set-Cookie")
			.allowCredentials(true)
			.maxAge(3600);
	}
}
