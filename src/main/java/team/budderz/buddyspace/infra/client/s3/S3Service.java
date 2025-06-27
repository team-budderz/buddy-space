package team.budderz.buddyspace.infra.client.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import team.budderz.buddyspace.global.exception.BaseException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");

    /**
     * S3에 MultipartFile 업로드
     *
     * @param file       업로드할 파일
     * @param uploaderId 업로더 ID
     * @param directory     S3 디렉토리
     * @return 업로드된 S3 객체의 key
     */
    public String upload(MultipartFile file, Long uploaderId, S3Directory directory) {
        if (file.isEmpty()) {
            throw new BaseException(S3ErrorCode.FILE_NOT_FOUND);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BaseException(S3ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String originalFileName = file.getOriginalFilename();
        // 파일 이름이 null 이거나 확장자가 없으면 예외
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new BaseException(S3ErrorCode.INVALID_FILE_NAME);
        }

        String extension = originalFileName.substring(originalFileName.lastIndexOf(".")); // 확장자
        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf(".")); // 파일명
        String timeStamp = LocalDateTime.now().format(FORMATTER);

        // 저장 경로 키 생성 = 디렉토리/파일명_업로더ID_타임스탬프.확장자
        String key = String.format("%s/%s_%d_%s%s", directory.getPath(), baseName, uploaderId, timeStamp, extension);

        try {
            // 업로드 요청 객체 생성
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            // 실제 S3 업로드
            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("S3 file upload 성공 - key: {}", key);

            return key;

        } catch (IOException e) {
            log.error("S3 file upload 실패 - key: {}", key);
            throw new BaseException(S3ErrorCode.UPLOAD_FAILED);
        }
    }

    /**
     * 바이트 배열 업로드 - 썸네일용
     *
     * @param bytes       업로드할 파일 데이터
     * @param key         저장할 S3 객체 key
     * @param contentType 파일 유형
     * @return 업로드된 S3 객체의 key
     */
    public String upload(byte[] bytes, String key, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(bytes));
        return key;
    }

    /**
     * S3 에 저장된 객체 삭제
     *
     * @param key 삭제할 S3 객체 key
     */
    public void delete(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("S3 file delete 성공 - key: {}", key);

        } catch (S3Exception e) {
            log.error("S3 file delete 실패 - key: {}", key);
            throw new BaseException(S3ErrorCode.DELETE_FAILED);
        }
    }

    /**
     * 다운로드용 presigned url 생성
     *
     * @param key              S3 객체 key
     * @param originalFilename 다운로드 시 보여질 파일 이름
     * @return 다운로드용 url (유효시간 10분)
     */
    public String generateDownloadUrl(String key, String originalFilename) {
        try {
            // 한글 파일명을 URL 인코딩
            String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20"); // 공백을 %20으로 변경

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    // UTF-8로 인코딩된 파일명 사용
                    .responseContentDisposition("attachment; filename*=UTF-8''" + encodedFilename)
                    .build();

            Duration expiration = Duration.ofMinutes(10);

            String url = s3Presigner.presignGetObject(builder -> builder
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(expiration)
            ).url().toString();

            log.info("S3 file download url 생성 성공 - url: {}", url);
            return url;

        } catch (S3Exception e) {
            log.error("S3 file download url 생성 실패 - key: {}", key);
            throw new BaseException(S3ErrorCode.GENERATE_URL_FAILED);
        }
    }

    /**
     * 조회용 url 생성
     *
     * @param key S3 객체 key
     * @return 조회용 url (유효시간 1시간)
     */
    public String generateViewUrl(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            Duration expiration = Duration.ofHours(1); // 유효시간 1시간

            String url = s3Presigner.presignGetObject(builder -> builder
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(expiration)
            ).url().toString();

            log.info("S3 file view url 생성 성공 - url: {}", url);
            return url;

        } catch (S3Exception e) {
            log.error("S3 file view url 생성 실패 - key: {}", key);
            throw new BaseException(S3ErrorCode.GENERATE_URL_FAILED);
        }
    }

    /**
     * 특정 디렉토리의 모든 S3 객체 key 목록 조회
     *
     * @param prefix S3 디렉토리 경로
     * @return 해당 경로의 파일 key 목록
     */
    public List<String> listObject(String prefix) {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);

            List<String> list = response.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());

            log.info("S3 file 목록 조회 성공 - list size: {}", list.size());
            return list;

        } catch (Exception e) {
            log.error("S3 file 목록 조회 실패 - prefix: {}", prefix);
            throw new BaseException(S3ErrorCode.LIST_FAILED);
        }
    }

    /**
     * 객체 존재 여부 확인
     *
     * @param key 파일 key
     * @return 파일 존재 여부
     */
    public boolean exists(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;

        } catch (S3Exception e) {
            log.error("S3 file 확인 실패 - key: {}", key);
            throw new BaseException(S3ErrorCode.CHECK_FAILED);
        }
    }
}
