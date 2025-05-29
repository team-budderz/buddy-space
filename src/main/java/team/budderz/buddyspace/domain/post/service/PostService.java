package team.budderz.buddyspace.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.budderz.buddyspace.api.post.request.SavePostRequest;
import team.budderz.buddyspace.api.post.response.SavePostResponse;
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

    public SavePostResponse savePost(
            Long groupId,
            SavePostRequest request
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(PostErrorCode.GROUP_ID_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
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

}
