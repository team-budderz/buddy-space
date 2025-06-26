package team.budderz.buddyspace.infra.database.post.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.budderz.buddyspace.api.post.response.FindsNoticePostResponse;
import team.budderz.buddyspace.api.post.response.FindsPostResponse;
import team.budderz.buddyspace.infra.database.comment.entity.QComment;
import team.budderz.buddyspace.infra.database.post.entity.QPost;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    // 게시글 전체 조회(예약된 게시글 제외)
    @Override
    public List<FindsPostResponse> findsPost(Long groupId, int offset, int pageSize) {
        QPost post = QPost.post;
        QComment comment = QComment.comment;

        List<Tuple> tuples = jpaQueryFactory
                .select(
                        post.id,
                        post.user.profileAttachment.id,
                        post.user.name,
                        post.createdAt,
                        post.content,
                        comment.id.count()
                )
                .from(post)
                .leftJoin(post.comments, comment)
                .where(
                        post.group.id.eq(groupId),
                        post.reserveAt.isNull().or(post.reserveAt.loe(LocalDateTime.now()))
                )
                .groupBy(
                        post.id,
                        post.user.profileAttachment.id,
                        post.user.name,
                        post.createdAt,
                        post.content
                )
                .orderBy(post.createdAt.desc())
                .offset(offset)
                .limit(pageSize)
                .fetch();

        return tuples.stream()
                .map(tuple -> toFindsPostResponse(tuple, post, comment))
                .toList();
    }

    // 게시글 공지 조회(내용 일부) (예약된 게시글 제외)
    @Override
    public List<FindsNoticePostResponse> findsShortNoticePost(Long groupId) {
        QPost post = QPost.post;

        List<Tuple> tuples = jpaQueryFactory
                .select(post.id, post.content)  // ← 수정
                .from(post)
                .where(
                        post.group.id.eq(groupId),
                        post.isNotice.isTrue(),
                        post.reserveAt.isNull().or(post.reserveAt.loe(LocalDateTime.now()))
                )
                .orderBy(post.createdAt.desc())
                .fetch();

        return tuples.stream()
                .map(tuple -> {
                    Long postId = tuple.get(post.id);
                    String content = tuple.get(post.content);
                    String shortContent = content.length() > 20 ? content.substring(0, 20) + "···" : content;
                    return new FindsNoticePostResponse(postId, shortContent);
                })
                .toList();

    }

    // 게시글 공지 조회(예약된 게시글 제외)
    @Override
    public List<FindsPostResponse> findsNoticePost(Long groupId) {
        QPost post = QPost.post;
        QComment comment = QComment.comment;

        List<Tuple> tuples = jpaQueryFactory
                .select(
                        post.id,
                        post.user.profileAttachment.id,
                        post.user.name,
                        post.createdAt,
                        post.content,
                        comment.id.count()
                )
                .from(post)
                .leftJoin(post.comments, comment)
                .where(
                        post.group.id.eq(groupId),
                        post.isNotice.isTrue(),
                        post.reserveAt.isNull().or(post.reserveAt.loe(LocalDateTime.now()))
                )
                .groupBy(
                        post.id,
                        post.user.profileAttachment.id,
                        post.user.name,
                        post.createdAt,
                        post.content
                )
                .orderBy(post.createdAt.desc())
                .fetch();

        return tuples.stream()
                .map(tuple -> toFindsPostResponse(tuple, post, comment))
                .toList();
    }

    private FindsPostResponse toFindsPostResponse(Tuple tuple, QPost post, QComment comment) {
        return new FindsPostResponse(
                tuple.get(post.user.profileAttachment.id),
                tuple.get(post.id),
                null,
                tuple.get(post.user.name),
                tuple.get(post.createdAt),
                tuple.get(post.content),
                tuple.get(comment.id.count())
        );
    }
}
