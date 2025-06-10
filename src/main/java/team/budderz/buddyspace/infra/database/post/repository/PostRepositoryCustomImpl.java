package team.budderz.buddyspace.infra.database.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.budderz.buddyspace.api.post.response.FindsNoticePostResponse;
import team.budderz.buddyspace.api.post.response.FindsPostResponse;
import team.budderz.buddyspace.infra.database.post.entity.QPost;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    // 게시글 전체 조회(예약된 게시글 제외)
    @Override
    public List<FindsPostResponse> findsPost(Long groupId, int offset, int pageSize) {
        QPost post = QPost.post;

        List<FindsPostResponse> results = jpaQueryFactory
                .select(Projections.constructor(FindsPostResponse.class,
                        post.user.imageUrl,
                        post.user.name,
                        post.createdAt,
                        post.content,
                        post.comments.size().longValue()
                        ))
                .from(post)
                .where(
                        post.group.id.eq(groupId),
                        post.reserveAt.loe(LocalDateTime.now())
                )
                .orderBy(post.createdAt.desc())
                .offset(offset)
                .limit(10)
                .fetch();

        return results;
    }

    // 게시글 공지 조회(내용 일부) (예약된 게시글 제외)
    @Override
    public List<FindsNoticePostResponse> findsShortNoticePost(Long groupId) {
        QPost post = QPost.post;

        List<String> contents  = jpaQueryFactory
                .select(post.content)
                .from(post)
                .where(
                        post.group.id.eq(groupId),
                        post.isNotice.isTrue(),
                        post.reserveAt.loe(LocalDateTime.now())
                )
                .orderBy(post.createdAt.desc())
                .fetch();

        return contents .stream()
                .map(content -> {
                    String shortcontent = content.length() > 20 ? content.substring(0, 20) + "···" : content;
                    return new FindsNoticePostResponse(shortcontent);
                })
                .toList();
    }
}
