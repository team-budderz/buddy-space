package team.budderz.buddyspace.infra.database.post.repository;

import team.budderz.buddyspace.api.post.response.FindsPostResponse;
import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepositoryCustom {

    List<FindsPostResponse> findsPost(Long groupId, int offset, int pageSize);
}
