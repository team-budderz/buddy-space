package team.budderz.buddyspace.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        factory.setConnectTimeout(5000);             // 서버 연결 최대 시간
        factory.setReadTimeout(5000);                // 응답 읽기 최대 시간
        factory.setConnectionRequestTimeout(5000);   // 커넥션 풀에서 가져오는 시간

        return new RestTemplate(factory);
    }
}
