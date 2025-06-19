package team.budderz.buddyspace.domain.attachment.service;

import jodd.io.FileNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.jcodec.api.JCodecException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.budderz.buddyspace.api.attachment.response.AttachmentResponse;
import team.budderz.buddyspace.domain.attachment.exception.AttachmentErrorCode;
import team.budderz.buddyspace.domain.attachment.exception.AttachmentException;
import team.budderz.buddyspace.domain.user.exception.UserErrorCode;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.infra.client.constant.S3Directory;
import team.budderz.buddyspace.infra.client.service.S3Service;
import team.budderz.buddyspace.infra.client.util.ThumbnailGenerator;
import team.budderz.buddyspace.infra.database.attachment.entity.Attachment;
import team.budderz.buddyspace.infra.database.attachment.repository.AttachmentRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final S3Service s3Service;
    private final UserRepository userRepository;

    private final Tika tika = new Tika(); // 파일 내용 기반 MIME 타입 판별기

    /**
     * 게시글 첨부파일 업로드
     *
     * @param file       업로드할 파일
     * @param uploaderId 업로더 ID
     * @return 업로드된 첨부파일 정보
     */
    @Transactional
    public AttachmentResponse upload(MultipartFile file, Long uploaderId) {
        // 파일 유형 기반으로 S3 디렉토리 결정
        String directory = determineDirectory(file);
        return upload(file, uploaderId, directory);
    }

    /**
     * 첨부파일 업로드
     * - 동영상의 경우 썸네일 생성하여 함께 저장
     *
     * @param file       업로드할 파일
     * @param uploaderId 업로더 ID
     * @param directory  저장할 S3 디렉토리 경로
     * @return 업로드된 첨부파일 정보
     */
    @Transactional
    public AttachmentResponse upload(MultipartFile file, Long uploaderId, String directory) {
        // 파일 null 이거나 비어 있으면 예외
        if (file == null || file.isEmpty()) {
            throw new AttachmentException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND);
        }

        // 업로더 사용자 조회
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // S3 에 실제 파일 업로드 후 key 반환
        String key = s3Service.upload(file, uploaderId, directory);

        String thumbnailKey = null;
        // 동영상 파일이면 썸네일 생성
        if (isVideo(file)) {
            thumbnailKey = uploadThumbnail(file, key);
        }

        Attachment attachment = Attachment.builder()
                .key(key)
                .filename(file.getOriginalFilename())
                .size(file.getSize())
                .contentType(getMimeType(file))
                .uploader(uploader)
                .thumbnailKey(thumbnailKey)
                .build();

        // DB 에 메타데이터 저장
        Attachment saved = attachmentRepository.save(attachment);

        // 조회용 url 생성
        String viewUrl = s3Service.generateViewUrl(saved.getKey());
        String thumbnailUrl = Optional.ofNullable(saved.getThumbnailKey())
                .map(s3Service::generateViewUrl)
                .orElse(null);

        return AttachmentResponse.of(saved, viewUrl, thumbnailUrl);
    }

    /**
     * 첨부파일 단건 정보 조회
     *
     * @param attachmentId 첨부파일 ID
     * @return 첨부파일 정보 - 조회용 url 포함
     */
    @Transactional(readOnly = true)
    public AttachmentResponse getAttachmentDetail(Long attachmentId) {
        Attachment attachment = findAttachmentOrThrow(attachmentId);

        String url = s3Service.generateViewUrl(attachment.getKey());
        String thumbnailUrl = Optional.ofNullable(attachment.getThumbnailKey())
                .map(s3Service::generateViewUrl)
                .orElse(null);

        return AttachmentResponse.of(attachment, url, thumbnailUrl);
    }

    /**
     * 조회용 url 조회
     *
     * @param attachmentId 첨부파일 ID
     * @return 조회용 url
     */
    @Transactional(readOnly = true)
    public String getViewUrl(Long attachmentId) {
        Attachment attachment = findAttachmentOrThrow(attachmentId);
        return s3Service.generateViewUrl(attachment.getKey());
    }

    /**
     * 다운로드 url 조회
     *
     * @param attachmentId 첨부파일 ID
     * @return 다운로드용 url
     */
    @Transactional(readOnly = true)
    public String getDownloadUrl(Long attachmentId) {
        Attachment attachment = findAttachmentOrThrow(attachmentId);
        return s3Service.generateDownloadUrl(attachment.getKey(), attachment.getFilename());
    }

    /**
     * 첨부파일 삭제
     *
     * @param attachmentId 첨부파일 ID
     */
    @Transactional
    public void delete(Long attachmentId) {
        Attachment attachment = findAttachmentOrThrow(attachmentId);

        // S3 원본 파일 삭제
        try {
            s3Service.delete(attachment.getKey());
        } catch (Exception e) {
            log.warn("S3 원본 파일 삭제 실패: {}", attachment.getKey(), e);
        }

        // 썸네일 존재하면 함께 삭제
        Optional.ofNullable(attachment.getThumbnailKey()).ifPresent(thumbnailKey -> {
            try {
                s3Service.delete(thumbnailKey);
            } catch (Exception e) {
                log.warn("S3 썸네일 삭제 실패: {}", thumbnailKey, e);
            }
        });

        // DB 정보 삭제
        attachmentRepository.delete(attachment);
    }

    /**
     * 첨부파일 조회
     *
     * @param attachmentId 첨부파일 ID
     * @return 첨부파일 엔티티
     */
    public Attachment findAttachmentOrThrow(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new AttachmentException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
    }

    /**
     * 실제 MIME 타입 기반 S3 디렉토리 결정 - 게시글 첨부파일용
     *
     * @param file 업로드할 파일
     * @return S3 디렉토리 경로
     */
    private String determineDirectory(MultipartFile file) {
        String mimeType = getMimeType(file);

        if (mimeType != null && mimeType.startsWith("image/")) {
            return S3Directory.ATTACHMENT_IMAGE;
        }
        if (mimeType != null && mimeType.startsWith("video/")) {
            return S3Directory.ATTACHMENT_VIDEO;
        }
        return S3Directory.ATTACHMENT_FILE;
    }

    private String getMimeType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream()); // 파일 내용 기반으로 MIME 타입 감지

        } catch (IOException e) {
            log.warn("파일 MIME 타입 감지 실패: {}", file.getOriginalFilename(), e);
            return Optional.ofNullable(file.getContentType()) // 클라이언트가 보낸 파일 유형이 있다면 사용
                    .orElse("application/octet-stream"); // 없으면 기본값
        }
    }

    private boolean isVideo(MultipartFile file) {
        String mimeType = getMimeType(file);
        return mimeType.startsWith("video/");
    }

    /**
     * 동영상 썸네일 생성 및 S3 업로드
     *
     * @param file     업로드한 동영상 파일
     * @param videoKey 원본 동영상의 S3 Key
     * @return 썸네일의 S3 Key
     */
    private String uploadThumbnail(MultipartFile file, String videoKey) {
        File tempVideo = null;
        try {
            tempVideo = File.createTempFile("video", null);
            file.transferTo(tempVideo);

            BufferedImage thumbnail = ThumbnailGenerator.generate(tempVideo);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", os);
            byte[] imageBytes = os.toByteArray();

            String baseName = FileNameUtil.getBaseName(videoKey);
            String thumbnailKey = S3Directory.THUMBNAIL + "/" + baseName + "_thumbnail.jpg";

            s3Service.upload(imageBytes, thumbnailKey, "image/jpeg");
            return thumbnailKey;

        } catch (IOException | JCodecException e) {
            log.warn("썸네일 생성 실패: {}", file.getOriginalFilename(), e);
            return null;

        } finally {
            if (tempVideo != null && tempVideo.exists()) {
                tempVideo.delete();
            }
        }
    }
}
