package team.budderz.buddyspace.domain.attachment.service;

import jodd.io.FileNameUtil;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final S3Service s3Service;
    private final UserRepository userRepository;

    /**
     * 게시글 첨부파일 업로드
     *
     * @param file       업로드할 파일
     * @param uploaderId 업로더 ID
     * @return 업로드된 첨부파일 정보
     */
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
        if (file.getContentType() != null && file.getContentType().startsWith("video/")) {
            thumbnailKey = uploadThumbnail(file, key);
        }

        Attachment attachment = Attachment.builder()
                .key(key)
                .filename(file.getOriginalFilename())
                .size(file.getSize())
                .contentType(file.getContentType())
                .uploader(uploader)
                .thumbnailKey(thumbnailKey)
                .build();

        // DB 에 메타데이터 저장
        Attachment saved = attachmentRepository.save(attachment);

        // 조회용 url 생성
        String viewUrl = s3Service.generateViewUrl(saved.getKey());

        String thumbnailUrl = null;
        if (saved.getThumbnailKey() != null) {
            s3Service.generateViewUrl(saved.getThumbnailKey());
        }

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

        String thumbnailUrl = null;
        if (attachment.getThumbnailKey() != null) {
            thumbnailUrl = s3Service.generateViewUrl(attachment.getThumbnailKey());
        }

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
        s3Service.delete(attachment.getKey());

        // 썸네일 존재하면 함께 삭제
        if (attachment.getThumbnailKey() != null) {
            s3Service.delete(attachment.getThumbnailKey());
        }

        // DB 정보 삭제
        attachmentRepository.delete(attachment);
    }

    /**
     * 첨부파일 엔티티 조회
     *
     * @param attachmentId 첨부파일 ID
     * @return 첨부파일 엔티티
     */
    public Attachment findAttachmentOrThrow(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new AttachmentException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND));
    }

    /**
     * 파일 유형에 따라 S3 디렉토리 결정 - 게시글 첨부파일용
     *
     * @param file 업로드할 파일
     * @return S3 디렉토리 경로
     */
    private String determineDirectory(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType != null && contentType.startsWith("image/")) {
            return S3Directory.ATTACHMENT_IMAGE;
        }
        if (contentType != null && contentType.startsWith("video/")) {
            return S3Directory.ATTACHMENT_VIDEO;
        }
        return S3Directory.ATTACHMENT_FILE;
    }

    /**
     * 동영상 썸네일 생성 및 S3 업로드
     *
     * @param file     업로드한 동영상 파일
     * @param videoKey 원본 동영상의 S3 Key
     * @return 썸네일의 S3 Key
     */
    private String uploadThumbnail(MultipartFile file, String videoKey) {
        try {
            File tempVideo = File.createTempFile("video", null);
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
            throw new AttachmentException(AttachmentErrorCode.THUMBNAIL_GENERATION_FAILED);
        }
    }
}
