package team.budderz.buddyspace.infra.database.group.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.budderz.buddyspace.api.group.response.GroupListResponse;
import team.budderz.buddyspace.infra.database.group.entity.GroupAccess;
import team.budderz.buddyspace.infra.database.group.entity.GroupSortType;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;
import team.budderz.buddyspace.infra.database.group.entity.QGroup;
import team.budderz.buddyspace.infra.database.membership.entity.JoinStatus;
import team.budderz.buddyspace.infra.database.membership.entity.QMembership;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final long GROUP_FETCH_LIMIT = 100L;

    @Override
    public List<GroupListResponse> findGroupsByUser(Long userId) {
        QGroup group = QGroup.group;
        QMembership membership = QMembership.membership;

        return queryFactory
                .select(Projections.constructor(GroupListResponse.class,
                        group.id,
                        group.name,
                        group.description,
                        group.coverImageUrl,
                        group.type,
                        group.interest,
                        membership.id.count().as("memberCount")
                ))
                .from(group)
                .join(membership).on(
                        membership.group.eq(group),
                        membership.user.id.eq(userId),
                        membership.joinStatus.eq(JoinStatus.APPROVED)
                )
                .groupBy(group.id, membership.joinedAt)
                .orderBy(membership.joinedAt.desc())
                .fetch();
    }

    @Override
    public List<GroupListResponse> findOnlineGroups(GroupSortType sortType) {
        return findGroupsByCondition(Set.of(GroupType.ONLINE, GroupType.HYBRID), null, sortType);
    }

    @Override
    public List<GroupListResponse> findOfflineGroups(Neighborhood neighborhood, GroupSortType sortType) {
        return findGroupsByCondition(Set.of(GroupType.OFFLINE, GroupType.HYBRID), neighborhood, sortType);
    }

    private List<GroupListResponse> findGroupsByCondition(Set<GroupType> groupTypes,
                                                          @Nullable Neighborhood neighborhood,
                                                          GroupSortType sortType) {
        QGroup group = QGroup.group;
        QMembership membership = QMembership.membership;

        BooleanBuilder conditions = new BooleanBuilder();

        conditions.and(group.type.in(groupTypes));
        conditions.and(group.access.eq(GroupAccess.PUBLIC));

        if (neighborhood != null) {
            conditions.and(group.neighborhood.eq(neighborhood));
        }

        JPAQuery<GroupListResponse> query = queryFactory
                .select(Projections.constructor(GroupListResponse.class,
                        group.id,
                        group.name,
                        group.description,
                        group.coverImageUrl,
                        group.type,
                        group.interest,
                        membership.id.count().as("memberCount")
                ))
                .from(group)
                .leftJoin(membership).on(
                        membership.group.eq(group)
                                .and(membership.joinStatus.eq(JoinStatus.APPROVED))
                )
                .where(conditions)
                .groupBy(group.id);

        if (sortType == GroupSortType.POPULAR) {
            query.orderBy(membership.id.count().desc());
        } else {
            query.orderBy(group.createdAt.desc());
        }

        return query.limit(GROUP_FETCH_LIMIT).fetch();
    }
}
