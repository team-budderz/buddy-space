package team.budderz.buddyspace.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.comment.response.FindsCommentResponse;
import team.budderz.buddyspace.api.post.request.SavePostRequest;
import team.budderz.buddyspace.api.post.request.UpdatePostRequest;
import team.budderz.buddyspace.api.post.response.*;
import team.budderz.buddyspace.domain.attachment.service.PostAttachmentService;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.domain.notification.service.NotificationService;
import team.budderz.buddyspace.domain.post.event.PostEvent;
import team.budderz.buddyspace.domain.post.exception.PostErrorCode;
import team.budderz.buddyspace.domain.user.provider.UserProfileImageProvider;
import team.budderz.buddyspace.global.exception.BaseException;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;
import team.budderz.buddyspace.infra.database.comment.repository.CommentRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;
import team.budderz.buddyspace.infra.database.post.entity.Post;
import team.budderz.buddyspace.infra.database.post.repository.PostRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final GroupValidator validator;
    private final UserProfileImageProvider profileImageProvider;
    private final MembershipRepository membershipRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final PostAttachmentService postAttachmentService;

    // 게시글 저장
    @Transactional
    public SavePostResponse savePost(
            Long groupId,
            Long userId,
            SavePostRequest request
    ) {
        Group group = validator.findGroupOrThrow(groupId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(PostErrorCode.USER_ID_NOT_FOUND));

        validator.validatePermission(userId, groupId, PermissionType.CREATE_POST);

        if (request.isNotice()) {
            validator.validateLeader(userId, groupId);

            Long noticeNum = postRepository.countByGroupIdAndIsNoticeTrue(groupId);

            if (noticeNum >= 5) {
                throw new BaseException(PostErrorCode.NOTICE_LIMIT_EXCEEDED);
            }
        }

        Post post = Post.builder()
                .group(group)
                .user(user)
                .content(request.content())
                .reserveAt(request.reserveAt())
                .isNotice(request.isNotice())
                .build();

        postRepository.save(post);

        eventPublisher.publishEvent(new PostEvent(post, user));
        postAttachmentService.bindAttachmentsToPost(request.content(), post, userId); // 첨부파일 연결

        return SavePostResponse.from(post);
    }

    // 게시글 수정
    @Transactional
    public UpdatePostResponse updatePost(
            Long groupId,
            Long postId,
            Long userId,
            UpdatePostRequest request
    ) {
        Group group = validator.findGroupOrThrow(groupId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(PostErrorCode.POST_ID_NOT_FOUND));

        validator.validateOwner(userId, groupId, post.getUser().getId());

        // 공지일 경우, 리더만 수정 가능
        if (request.isNotice()) {
            if (!Objects.equals(userId, group.getLeader().getId())) {
                throw new BaseException(PostErrorCode.NOTICE_POST_ONLY_ALLOWED_BY_LEADER);
            }

            Long noticeNum = postRepository.countByGroupIdAndIsNoticeTrue(groupId);
            // 공지는 최대 5개까지
            if (noticeNum >= 5) {
                throw new BaseException(PostErrorCode.NOTICE_LIMIT_EXCEEDED);
            }
        }

        // 예약 글이면서, 예약 시간이 현재 이후 일때만 reserveAt 수정 허용
        boolean isResverable = post.getReserveAt() != null &&
                post.getReserveAt().isAfter(LocalDateTime.now());

        if (request.reserveAt() != null) {
            if (!isResverable) {
                throw new BaseException(PostErrorCode.NOT_ALLOWED_TO_EDIT_RESERVE_TIME);
            }

            post.updateReserveAt(request.reserveAt());
        }

        post.updatePost(request.content(), request.isNotice());
        // 기존 첨부파일 연결 삭제 및 고아 첨부파일 정리
        postAttachmentService.deletePostAttachmentsForUpdate(post);
        // 새 content 기준으로 첨부파일 재연결
        postAttachmentService.bindAttachmentsToPost(request.content(), post, userId);

        return UpdatePostResponse.from(post);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(
            Long groupId,
            Long postId,
            Long userId
    ) {
        validator.findGroupOrThrow(groupId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(PostErrorCode.POST_ID_NOT_FOUND));

        validator.validatePermission(userId, groupId, PermissionType.DELETE_POST, post.getUser().getId());

        postAttachmentService.deletePostFiles(post); // 게시글 첨부파일 삭제
        postRepository.delete(post);
    }

    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public List<FindsPostResponse> findsPost(
            Long groupId,
            int page
    ) {
        int pageSize = 10;
        int offset = page * pageSize;

        List<FindsPostResponse> result = postRepository.findsPost(groupId, offset, pageSize);
        return enrichPostResponses(result);
    }

    // 게시글 공지 조회(내용 일부)
    @Transactional(readOnly = true)
    public List<FindsNoticePostResponse> findNoticePostSummaries(
            Long groupId
    ) {
        return postRepository.findsShortNoticePost(groupId);
    }

    // 게시글 공지 조회
    @Transactional(readOnly = true)
    public List<FindsPostResponse> findsNoticePost(
            Long groupId
    ) {
        List<FindsPostResponse> result = postRepository.findsNoticePost(groupId);
        return enrichPostResponses(result);
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public FindPostResponse findPost(Long groupId, Long postId) {
        validator.findGroupOrThrow(groupId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(PostErrorCode.POST_ID_NOT_FOUND));

        List<Comment> allComments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        List<Comment> topLevelComments = allComments.stream()
                .filter(c -> c.getParent() == null)
                .toList();

        String postUserImgUrl = profileImageProvider.getProfileImageUrl(post.getUser());

        List<FindsCommentResponse> commentResponses = topLevelComments.stream()
                .map(comment -> {
                    String commentUserImgUrl = profileImageProvider.getProfileImageUrl(comment.getUser());
                    return FindsCommentResponse.of(
                            comment.getId(),
                            comment.getUser().getId(),
                            commentUserImgUrl,
                            comment.getUser().getName(),
                            comment.getContent(),
                            comment.getCreatedAt(),
                            (long) comment.getChildren().size()
                    );
                })
                .toList();

        // 첨부파일 URL 포함한 content 로 변환 (조회용)
        String renderedContent = postAttachmentService.renderPostContent(post.getContent());

        return FindPostResponse.of(
                post.getUser().getId(),
                postUserImgUrl,
                post.getUser().getName(),
                post.getCreatedAt(),
                renderedContent,
                post.getIsNotice(),
                (long) post.getComments().size(),
                commentResponses
        );
    }

    private List<FindsPostResponse> enrichPostResponses(List<FindsPostResponse> result) {
        return result.stream()
                .map(post -> {
                    // 프로필 이미지 URL 생성
                    String profileUrl = profileImageProvider.getProfileImageUrl(post.profileAttachmentId());
                    // content 렌더링
                    String renderedContent = postAttachmentService.renderPostContent(post.content());
                    // 프로필 이미지와 렌더링된 content 반영
                    return post.withProfileImageUrl(profileUrl)
                            .withRenderedContent(renderedContent);
                })
                .toList();
    }
}
