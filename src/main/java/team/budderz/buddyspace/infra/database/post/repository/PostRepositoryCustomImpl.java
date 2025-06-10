package team.budderz.buddyspace.infra.database.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.budderz.buddyspace.api.post.response.FindsPostResponse;
import team.budderz.buddyspace.infra.database.post.entity.QPost;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

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
}
