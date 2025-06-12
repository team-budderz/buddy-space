package team.budderz.buddyspace.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${aws.s3.access.key}")
    private String accessKey;

    @Value("${aws.s3.secret.key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // 자격 증명 생성
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        // S3Client는 서버 내부에서 직접 S3 객체 업로드/다운로드/삭제 등에 사용됨
        return S3Client.builder()
                .region(Region.of(region)) // 사용할 S3 지역 설정
                .credentialsProvider(StaticCredentialsProvider.create(credentials)) // 정적 자격 증명 설정
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        // S3Presigner는 프리사인드 URL(일정 시간 동안 유효한 S3 접근 URL) 생성에 사용됨
        // 클라이언트(프론트엔드)에서 직접 업로드/다운로드하도록 링크 제공할 때 활용
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
