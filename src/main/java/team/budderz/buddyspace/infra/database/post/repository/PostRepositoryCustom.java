package team.budderz.buddyspace.infra.database.post.repository;

import team.budderz.buddyspace.api.post.response.FindsNoticePostResponse;
import team.budderz.buddyspace.api.post.response.FindsPostResponse;

import java.util.List;

public interface PostRepositoryCustom {

    List<FindsPostResponse> findsPost(Long groupId, int offset, int pageSize);

    List<FindsNoticePostResponse>  findsShortNoticePost(Long groupId);

    List<FindsPostResponse> findsNoticePost(Long groupId);
}
