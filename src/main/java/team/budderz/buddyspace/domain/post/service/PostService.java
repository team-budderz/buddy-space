package team.budderz.buddyspace.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.post.request.SavePostRequest;
import team.budderz.buddyspace.api.post.request.UpdatePostRequest;
import team.budderz.buddyspace.api.post.response.SavePostResponse;
import team.budderz.buddyspace.api.post.response.UpdatePostResponse;
import team.budderz.buddyspace.domain.post.exception.PostErrorCode;
import team.budderz.buddyspace.global.exception.BaseException;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.post.entity.Post;
import team.budderz.buddyspace.infra.database.post.repository.PostRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

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

        Post post = Post.builder()
                .group(group)
                .user(user)
                .content(request.getContent())
                .reserveAt(request.getReserveAt())
                .isNotice(request.getIsNotice())
                .build();

        postRepository.save(post);
        return new SavePostResponse(post);
    }

    // 게시글 수정
    @Transactional
    public UpdatePostResponse updatePost(
            Long groupId,
            Long postId,
            Long userId,
            UpdatePostRequest request
    ) {

        // 인증, 인가 부분이 완료되면 로그인 한 유저가 게시글 작성자가 맞는지 아이디 조회
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(PostErrorCode.GROUP_ID_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(PostErrorCode.POST_ID_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(PostErrorCode.USER_ID_NOT_FOUND));

        post.updatePost(request.getContent(), request.getIsNotice());

        return new UpdatePostResponse(post);
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(PostErrorCode.USER_ID_NOT_FOUND));

        postRepository.delete(post);
        return null;
    }

}
