package team.budderz.buddyspace.domain.attachment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.budderz.buddyspace.api.attachment.response.AttachmentResponse;
import team.budderz.buddyspace.domain.attachment.exception.AttachmentErrorCode;
import team.budderz.buddyspace.domain.attachment.exception.AttachmentException;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.infra.client.s3.S3Directory;
import team.budderz.buddyspace.infra.database.attachment.entity.Attachment;
import team.budderz.buddyspace.infra.database.attachment.entity.PostAttachment;
import team.budderz.buddyspace.infra.database.attachment.repository.PostAttachmentRepository;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.post.entity.Post;
import team.budderz.buddyspace.infra.database.post.repository.PostRepository;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostAttachmentService {

    private final AttachmentService attachmentService;
    private final GroupValidator groupValidator;
    private final PostAttachmentRepository postAttachmentRepository;
    private final PostRepository postRepository;

    // 태그 전체 단위 매칭
    private static final Pattern DATA_ID_PATTERN =
            Pattern.compile("<(img|video|a)[^>]*data-id=\"(\\d+)\"[^>]*>(?:</\\1>)?");

    /**
     * 게시글 첨부파일 업로드
     *
     * @param file       업로드할 파일
     * @param groupId    모임 ID
     * @param uploaderId 업로더 ID
     * @return 업로드된 첨부파일 정보
     */
    @Transactional
    public AttachmentResponse uploadPostFiles(MultipartFile file, Long groupId, Long uploaderId) {
        // 게시글 생성 권한 검증
        groupValidator.validatePermission(uploaderId, groupId, PermissionType.CREATE_POST);
        // 파일 유형 기반으로 S3 디렉토리 결정
        S3Directory directory = determineDirectoryForPost(file);
        // 첨부파일 업로드 및 업로드된 파일 정보 반환
        return attachmentService.upload(file, uploaderId, directory);
    }

    /**
     * 게시글 content 에 포함된 첨부파일 ID(data-id) 를 추출해 PostAttachment 저장
     *
     * @param content    게시글 본문 (HTML 문자열)
     * @param post       저장할 게시글 엔티티
     * @param uploaderId 로그인한 사용자 ID (업로더)
     */
    public void bindAttachmentsToPost(String content, Post post, Long uploaderId) {
        // content 에서 data-id 값을 추출하여 attachmentIds Set 으로 전환
        Set<Long> attachmentIds = extractAttachmentIdsFromContent(content);
        if (attachmentIds.isEmpty()) return;

        // 각 ID에 해당하는 Attachment 리스트 반환
        List<Attachment> attachments = attachmentService.findAttachmentsByIds(attachmentIds);

        // 각 첨부파일 처리
        for (Attachment attachment : attachments) {
            // 첨부파일 업로더가 현재 로그인한 사용자가 아니면 건너뜀
            if (!Objects.equals(attachment.getUploader().getId(), uploaderId)) {
                continue;
            }
            // PostAttachment 생성 및 DB 저장
            PostAttachment postAttachment = PostAttachment.of(post, attachment);
            postAttachmentRepository.save(postAttachment);
        }
    }

    /**
     * 게시글 본문(content)에 포함된 첨부파일 태그(img, video, a)의 data-id를 기반으로
     * URL 및 정보를 담아서 실제 조회 가능한 HTML 형태로 변환
     *
     * @param content 게시글 본문 HTML 문자열
     * @return 실제 URL 포함하여 변환된 HTML 문자열
     */
    public String renderPostContent(String content) {
        if (content == null || !content.contains("data-id=")) return content;

        // data-id 가 포함된 태그를 찾기 위한 정규표현식 Matcher
        Matcher matcher = DATA_ID_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        // 정규표현식과 매치되는 태그가 있는 동안 반복
        while (matcher.find()) {
            // matcher.group(1): 태그 이름 (img, video, a 중 하나)
            String tagName = matcher.group(1);
            Long id;
            try {
                // matcher.group(2): data-id 안의 숫자 문자열 → Long
                id = Long.parseLong(matcher.group(2));
            } catch (NumberFormatException e) {
                // 실패하면 무시하고 다음 태그로 넘어감
                continue;
            }

            try {
                Attachment attachment = attachmentService.findAttachmentOrThrow(id);
                String url = attachmentService.getViewUrl(id);
                String contentType = attachment.getContentType();
                String replacement;

                log.info("매칭된 파일 정보: tag={}, id={}, contentType={}", tagName, id, contentType);

                // 이미지인 경우
                // <img data-id="13"> → <img data-id="13" src="..." />
                if ("img".equals(tagName) && contentType.startsWith("image/")) {
                    replacement = String.format("<img data-id=\"%d\" src=\"%s\" />", id, url);

                    // 비디오인 경우
                    // <video data-id="13"></video> → <video ...><source src="..." type="..." /></video>
                } else if ("video".equals(tagName) && contentType.startsWith("video/")) {
                    String thumbnailUrl = null;

                    // 썸네일 키가 있으면 썸네일용 URL도 생성
                    if (attachment.getThumbnailKey() != null) {
                        thumbnailUrl = attachmentService.getViewUrlByKey(attachment.getThumbnailKey());
                    }

                    // <video> 태그 생성 (poster는 썸네일이 있는 경우만 포함)
                    replacement = String.format(
                            "<video data-id=\"%d\" controls%s>" +
                                    "<source src=\"%s\" type=\"%s\" />" +
                                    "</video>",
                            id,
                            thumbnailUrl != null ? " poster=\"" + thumbnailUrl + "\"" : "",
                            url,
                            contentType
                    );

                    // 파일인 경우
                    // <a data-id="13" href="..." data-name="..." data-size="..." data-type="..." download>파일명 (사이즈)</a>
                } else if ("a".equals(tagName)) {
                    // 다운로드용 URL 생성
                    String downloadUrl = attachmentService.getDownloadUrl(id);
                    // 파일 이름, 파일 크기 문자열 변환
                    String filename = attachment.getFilename();
                    String fileSize = convertFileSize(attachment.getSize());

                    replacement = String.format(
                            "<a data-id=\"%d\" href=\"%s\" data-name=\"%s\" data-size=\"%s\" data-type=\"%s\" download>" +
                                    "%s (%s)</a>",
                            id, downloadUrl, filename, fileSize, contentType, filename, fileSize
                    );

                    // 어떤 조건에도 해당하지 않으면 원본 태그 그대로 사용
                } else {
                    replacement = matcher.group(0);
                }
                // 변환된 태그를 결과 버퍼에 추가
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));

            } catch (Exception e) {
                log.warn("첨부파일 변환 중 오류: tag={}, id={}", matcher.group(1), matcher.group(2), e);
                // 오류가 난 태그는 원본 그대로 유지
                matcher.appendReplacement(sb, matcher.group(0));
            }
        }
        // 마지막 매칭 이후의 나머지 문자열도 추가
        matcher.appendTail(sb);
        // 최종 변환된 HTML 반환
        return sb.toString();
    }

    /**
     * 게시글 첨부파일 연결 정보 제거 및 고아 첨부파일 정리
     *
     * @param post 게시글 엔티티
     */
    @Transactional
    public void deletePostAttachmentsForUpdate(Post post) {
        // 본문에서 사용 중인 data-id 파싱
        Set<Long> usedAttachmentIds = extractAttachmentIdsFromContent(post.getContent());

        // 기존 게시글에 연결된 첨부파일 전체 조회
        List<PostAttachment> postAttachments = postAttachmentRepository.findAllByPost(post);

        // 첨부파일 연결이 없으면 바로 종료
        if (postAttachments.isEmpty()) return;

        // 삭제 대상 Attachment ID 수집
        List<Long> orphanAttachmentIds = new ArrayList<>();
        for (PostAttachment postAttachment : postAttachments) {
            Long attachmentId = postAttachment.getAttachment().getId();
            if (!usedAttachmentIds.contains(attachmentId)) {
                orphanAttachmentIds.add(attachmentId);
            }
        }

        // 기존 연결 정보 삭제
        postAttachmentRepository.deleteAllByPost(post);

        // 고아 첨부파일 삭제 (S3 + DB)
        for (Long orphanAttachmentId : orphanAttachmentIds) {
            attachmentService.delete(orphanAttachmentId);
        }
    }

    /**
     * 게시글 첨부파일 전체 삭제
     *
     * @param post 게시글 엔티티
     */
    @Transactional
    public void deletePostFiles(Post post) {
        List<PostAttachment> postAttachments = postAttachmentRepository.findAllByPost(post);

        // 첨부파일 연결이 없으면 바로 종료
        if (postAttachments.isEmpty()) return;

        List<Long> attachmentIds = postAttachments.stream()
                .map(postAttachment -> postAttachment.getAttachment().getId())
                .collect(Collectors.toList());

        // 중간 테이블 연결 데이터 전체 삭제
        postAttachmentRepository.deleteAllByPost(post);
        // 게시글에 첨부된 파일 S3, DB 삭제
        attachmentService.deleteAttachments(attachmentIds);
    }

    /**
     * 특정 모임의 모든 게시글에 첨부된 사진/영상 파일 목록 조회
     *
     * @param groupId     모임 ID
     * @param loginUserId 로그인한 사용자 ID
     * @param type        조회할 파일 타입
     * @return 조회된 파일 목록
     */
    public List<AttachmentResponse> findGroupAlbum(Long groupId, Long loginUserId, String type) {
        validateType(type);
        groupValidator.validateMember(loginUserId, groupId);

        List<Post> posts = postRepository.findByGroup_Id(groupId);
        if (posts.isEmpty()) return List.of();

        // 게시글에 연결된 모든 첨부파일 조회
        List<PostAttachment> attachments = postAttachmentRepository.findAllByPostIn(posts);

        return attachments.stream()
                .map(PostAttachment::getAttachment)
                .filter(attachment -> {
                    if (type == null) {
                        return isImageOrVideo(attachment);
                    }
                    return type.equals("image") && isImage(attachment)
                            || type.equals("video") && isVideo(attachment);
                })
                .map(attachment -> {
                    String url = attachmentService.getViewUrl(attachment.getId());
                    String thumbnailUrl = null;

                    if (attachment.getThumbnailKey() != null) {
                        thumbnailUrl = attachmentService.getViewUrlByKey(attachment.getThumbnailKey());
                    }

                    return AttachmentResponse.of(attachment, url, thumbnailUrl);
                })
                .toList();
    }

    // content 내의 data-id 값들을 추출하여 반환
    private Set<Long> extractAttachmentIdsFromContent(String content) {
        Set<Long> ids = new HashSet<>();
        if (content == null || !content.contains("data-id=")) return ids;

        Matcher matcher = DATA_ID_PATTERN.matcher(content);
        while (matcher.find()) {
            try {
                ids.add(Long.parseLong(matcher.group(2)));
            } catch (NumberFormatException ignored) {
                // 숫자 파싱에 실패한 경우 무시 (비정상적인 data-id 방어용)
            }
        }
        return ids;
    }

    // 실제 MIME 타입 기반 S3 디렉토리 결정
    private S3Directory determineDirectoryForPost(MultipartFile file) {
        String mimeType = attachmentService.getMimeType(file);

        if (mimeType != null && mimeType.startsWith("image/")) {
            return S3Directory.POST_IMAGE;
        }
        if (mimeType != null && mimeType.startsWith("video/")) {
            return S3Directory.POST_VIDEO;
        }
        return S3Directory.POST_FILE;
    }

    // 파일 크기(byte)를 읽기 쉬운 형식(B, KB, MB)으로 변환 - MB 이하만 처리
    private String convertFileSize(long size) {
        if (size <= 0) return "0 B"; // 0 이하면 "0 B"
        if (size < 1024) return size + " B"; // 1KB 미만은 byte로 표시

        double kb = size / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb); // 1MB 미만은 KB로

        double mb = kb / 1024.0;
        return String.format("%.1f MB", mb); // 그 외는 MB로
    }

    // type 값이 허용된 값인지 확인 (image, video, null만 허용)
    private void validateType(String type) {
        if (type == null) return;
        if (!type.equals("image") && !type.equals("video")) {
            throw new AttachmentException(AttachmentErrorCode.INVALID_TYPE);
        }
    }

    private boolean isImage(Attachment attachment) {
        return attachment.getContentType().startsWith("image/");
    }

    private boolean isVideo(Attachment attachment) {
        return attachment.getContentType().startsWith("video/");
    }

    private boolean isImageOrVideo(Attachment attachment) {
        return isImage(attachment) || isVideo(attachment);
    }
}
