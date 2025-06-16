package team.budderz.buddyspace.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${kakao.map.host}")
    private String kakaoHost;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl(kakaoHost)
                .build();
    }
}
