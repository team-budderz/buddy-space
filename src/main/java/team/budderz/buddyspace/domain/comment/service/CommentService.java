package team.budderz.buddyspace.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.comment.request.CommentRequest;
import team.budderz.buddyspace.api.comment.response.CommentResponse;
import team.budderz.buddyspace.api.comment.response.FindsRecommentResponse;
import team.budderz.buddyspace.api.comment.response.RecommentResponse;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.domain.comment.exception.CommentErrorCode;
import team.budderz.buddyspace.domain.notification.service.NotificationService;
import team.budderz.buddyspace.domain.user.provider.UserProfileImageProvider;
import team.budderz.buddyspace.global.exception.BaseException;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;
import team.budderz.buddyspace.infra.database.comment.repository.CommentRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;
import team.budderz.buddyspace.infra.database.post.entity.Post;
import team.budderz.buddyspace.infra.database.post.repository.PostRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final GroupRepository groupRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserProfileImageProvider profileImageProvider;
    private final NotificationService notificationService;

    // 댓글 저장
    @Transactional
    public CommentResponse saveComment(
            Long groupId,
            Long postId,
            Long userId,
            CommentRequest request
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.POST_ID_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.USER_ID_NOT_FOUND));

        if (post.doesNotBelongToGroup(groupId)) {
            throw new BaseException(CommentErrorCode.POST_NOT_BELONG_TO_GROUP);
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(request.content())
                .build();

        commentRepository.save(comment);

        // 알림 전송
        User postWriter = post.getUser();

        if(!postWriter.getId().equals(userId)) {
            NotificationArgs args = new NotificationArgs(
                    user.getName(),
                    post.getGroup().getName(),
                    null,
                    postWriter.getId(),
                    post.getGroup().getId(),
                    post.getId(),
                    comment.getId()
            );

            notificationService.sendNotice(
                    NotificationType.COMMENT,
                    postWriter,
                    post.getGroup(),
                    args
            );
        }

        return CommentResponse.from(comment);
    }

    // 대댓글 저장
    @Transactional
    public RecommentResponse saveRecomment(
            Long groupId,
            Long postId,
            Long commentId,
            Long userId,
            CommentRequest request
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.POST_ID_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.COMMENT_ID_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.USER_ID_NOT_FOUND));

        if (post.doesNotBelongToGroup(groupId)) {
            throw new BaseException(CommentErrorCode.POST_NOT_BELONG_TO_GROUP);
        }

        if (comment.doesNotBelongToPost(postId)) {
            throw new BaseException(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }

        Comment reComment = Comment.builder()
                .post(post)
                .user(user)
                .parent(comment)
                .content(request.content())
                .build();

        commentRepository.save(reComment);
        comment.getChildren().add(reComment);

        // 알림 전송
        User postWriter = post.getUser();
        User parentCommentWriter  = reComment.getParent().getUser();

        NotificationArgs argsForPostWriter  = new NotificationArgs(
                user.getName(),
                post.getGroup().getName(),
                null,
                postWriter.getId(),
                post.getGroup().getId(),
                post.getId(),
                reComment.getId()
        );

        NotificationArgs argsForParentCommentWriter = new NotificationArgs(
                user.getName(),
                post.getGroup().getName(),
                null,
                parentCommentWriter.getId(),
                post.getGroup().getId(),
                post.getId(),
                reComment.getId()
        );

        // 게시글 작성자에게 알림
        if (!postWriter.getId().equals(userId)) {
            notificationService.sendNotice(
                    NotificationType.REPLY,
                    postWriter,
                    post.getGroup(),
                    argsForPostWriter
            );
        }

        // 부모 댓글 작성자에게 알림 (작성자, 게시글 작성자 다를 때)
        if (!parentCommentWriter.getId().equals(userId)
                && !parentCommentWriter.getId().equals(postWriter.getId())) {
            notificationService.sendNotice(
                    NotificationType.REPLY,
                    parentCommentWriter,
                    post.getGroup(),
                    argsForParentCommentWriter
            );
        }
        return RecommentResponse.from(reComment);
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(
            Long groupId,
            Long postId,
            Long commentId,
            Long userId,
            CommentRequest request
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.POST_ID_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.COMMENT_ID_NOT_FOUND));

        if (post.doesNotBelongToGroup(groupId)) {
            throw new BaseException(CommentErrorCode.POST_NOT_BELONG_TO_GROUP);
        }

        if (comment.doesNotBelongToPost(postId)) {
            throw new BaseException(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }

        if (comment.isNotWrittenBy(userId)) {
            throw new BaseException(CommentErrorCode.UNAUTHORIZED_COMMENT_UPDATE);
        }

        comment.updateComment(request.content());
        return CommentResponse.from(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(
            Long groupId,
            Long postId,
            Long commentId,
            Long userId
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.GROUP_ID_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.POST_ID_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.COMMENT_ID_NOT_FOUND));

        if (post.doesNotBelongToGroup(groupId)) {
            throw new BaseException(CommentErrorCode.POST_NOT_BELONG_TO_GROUP);
        }

        if (comment.doesNotBelongToPost(postId)) {
            throw new BaseException(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }

        if (comment.isNotWrittenBy(userId)
                && !Objects.equals(group.getLeader().getId(), userId)) {
            throw new BaseException(CommentErrorCode.UNAUTHORIZED_COMMENT_DELETE);
        }

        commentRepository.delete(comment);
    }

    // 대댓글 조회
    @Transactional(readOnly = true)
    public List<FindsRecommentResponse> findsRecomment(
            Long groupId,
            Long postId,
            Long commentId
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.POST_ID_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.COMMENT_ID_NOT_FOUND));

        if (post.doesNotBelongToGroup(groupId)) {
            throw new BaseException(CommentErrorCode.POST_NOT_BELONG_TO_GROUP);
        }

        if (comment.doesNotBelongToPost(postId)) {
            throw new BaseException(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }

        List<Comment> comments = commentRepository.findByParentOrderByCreatedAtAsc(comment);

        return comments.stream()
                .map(recomment -> FindsRecommentResponse.from(
                        recomment,
                        profileImageProvider.getProfileImageUrl(recomment.getUser())
                ))
                .collect(Collectors.toList());
    }

}
