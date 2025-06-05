package team.budderz.buddyspace.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.comment.request.CommentRequest;
import team.budderz.buddyspace.api.comment.response.CommentResponse;
import team.budderz.buddyspace.api.comment.response.FindsRecommentResponse;
import team.budderz.buddyspace.api.comment.response.RecommentResponse;
import team.budderz.buddyspace.domain.comment.exception.CommentErrorCode;
import team.budderz.buddyspace.global.exception.BaseException;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;
import team.budderz.buddyspace.infra.database.comment.repository.CommentRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
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

    // 댓글 저장
    @Transactional
    public CommentResponse saveComment(
            Long groupId,
            Long postId,
            Long userId,
            CommentRequest request
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.GROUP_ID_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.POST_ID_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.USER_ID_NOT_FOUND));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(request.content())
                .build();

        commentRepository.save(comment);
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
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.GROUP_ID_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.POST_ID_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.COMMENT_ID_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.USER_ID_NOT_FOUND));

        Comment reComment = Comment.builder()
                .post(post)
                .user(user)
                .parent(comment)
                .content(request.content())
                .build();

        commentRepository.save(reComment);
        comment.getChildren().add(reComment);
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
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.GROUP_ID_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.POST_ID_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.COMMENT_ID_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.USER_ID_NOT_FOUND));

        if (!Objects.equals(comment.getPost().getId(), postId)) {
            throw new BaseException(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }

        if (!Objects.equals(comment.getUser().getId(), userId)) {
            throw new BaseException(CommentErrorCode.UNAUTHORIZED_COMMENT_UPDATE);
        }

        comment.updateComment(request.content());
        return CommentResponse.from(comment);
    }

    // 댓글 삭제
    @Transactional
    public Void deleteComment (
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.USER_ID_NOT_FOUND));

        if (!Objects.equals(comment.getPost().getId(), postId)) {
            throw new BaseException(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }

        if (!Objects.equals(comment.getUser().getId(), userId)
                && !Objects.equals(group.getLeader().getId(), userId)) {
            throw new BaseException(CommentErrorCode.UNAUTHORIZED_COMMENT_DELETE);
        }

        commentRepository.delete(comment);
        return null;
    }

    // 대댓글 조회
    @Transactional(readOnly = true)
    public List<FindsRecommentResponse> findsRecomment(
            Long groupId,
            Long postId,
            Long commentId
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.COMMENT_ID_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.POST_ID_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.COMMENT_ID_NOT_FOUND));

        if (!Objects.equals(comment.getPost().getId(), postId)) {
            throw new BaseException(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST);
        }

        List<Comment> comments = commentRepository.findByParentOrderByCreatedAtAsc(comment);

        return comments.stream()
                .map(FindsRecommentResponse::from)
                .collect(Collectors.toList());
    }

}
