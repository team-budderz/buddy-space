package team.budderz.buddyspace.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.post.request.SavePostRequest;
import team.budderz.buddyspace.api.post.request.UpdatePostRequest;
import team.budderz.buddyspace.api.post.response.*;
import team.budderz.buddyspace.domain.post.exception.PostErrorCode;
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
public class PostService {

    private final PostRepository postRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    // 게시글 저장
    @Transactional
    public SavePostResponse savePost(
            Long groupId,
            Long userId,
            SavePostRequest request
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(PostErrorCode.GROUP_ID_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(PostErrorCode.USER_ID_NOT_FOUND));

        if (request.isNotice()) {
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
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(PostErrorCode.GROUP_ID_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(PostErrorCode.POST_ID_NOT_FOUND));

        if (!Objects.equals(post.getUser().getId(), userId)) {
            throw new BaseException(PostErrorCode.UNAUTHORIZED_POST_UPDATE);
        }

        if (!post.getIsNotice() && request.isNotice()) {
            Long noticeNum = postRepository.countByGroupIdAndIsNoticeTrue(groupId);

            if (noticeNum >= 5) {
                throw new BaseException(PostErrorCode.NOTICE_LIMIT_EXCEEDED);
            }
        }

        post.updatePost(request.content(), request.isNotice());

        return UpdatePostResponse.from(post);
    }

    // 게시글 삭제
    @Transactional
    public Void deletePost (
            Long groupId,
            Long postId,
            Long userId
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(PostErrorCode.GROUP_ID_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(PostErrorCode.POST_ID_NOT_FOUND));

        if (!Objects.equals(post.getUser().getId(), userId)
                && !Objects.equals(group.getLeader().getId(), userId)) {
            throw new BaseException(PostErrorCode.UNAUTHORIZED_POST_DELETE);
        }

        postRepository.delete(post);
        return null;
    }

    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public List<FindsPostResponse> findsPost(
            Long groupId
    ) {
        List<Post> posts = postRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        return posts.stream()
                .map(FindsPostResponse::from)
                .collect(Collectors.toList());
    }

    // 게시글 공지 조회(내용 일부)
    @Transactional(readOnly = true)
    public List<FindsNoticePostResponse> findNoticePostSummaries(
            Long groupId
    ) {
        List<Post> posts = postRepository.findByGroupIdAndIsNoticeTrueOrderByCreatedAtDesc(groupId);

        return posts.stream()
                .map(FindsNoticePostResponse::from)
                .collect(Collectors.toList());
    }

    // 게시글 공지 조회
    @Transactional(readOnly = true)
    public List<FindsPostResponse> findsNoticePost(
            Long groupId
    ) {
        List<Post> posts = postRepository.findByGroupIdAndIsNoticeTrueOrderByCreatedAtDesc(groupId);

        return posts.stream()
                .map(FindsPostResponse::from)
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public FindPostResponse findPost(
            Long groupId,
            Long postId
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(PostErrorCode.GROUP_ID_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(PostErrorCode.POST_ID_NOT_FOUND));

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        return FindPostResponse.from(post, comments);

    }
}
