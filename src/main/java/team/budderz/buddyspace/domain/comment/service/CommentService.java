package team.budderz.buddyspace.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.comment.request.CommentRequest;
import team.budderz.buddyspace.api.comment.response.CommentResponse;
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

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    // 댓글 저장
    @Transactional
    public CommentResponse savePost(
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
                .content(request.getContent())
                .build();

        commentRepository.save(comment);
        return new CommentResponse(comment);
    }


}
