package team.budderz.buddyspace.infra.database.group.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import team.budderz.buddyspace.api.group.response.GroupListResponse;
import team.budderz.buddyspace.infra.database.group.entity.*;
import team.budderz.buddyspace.infra.database.membership.entity.JoinStatus;
import team.budderz.buddyspace.infra.database.membership.entity.QMembership;

import java.util.*;
import java.util.stream.Collectors;

import static com.querydsl.core.types.dsl.Expressions.stringTemplate;

@Repository
@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 내 모임 목록 조회
     *
     * @param userId   로그인 사용자 ID
     * @param pageable 페이징 정보
     * @return 조회된 모임 목록 정보
     */
    @Override
    public Page<GroupListResponse> findGroupsByUser(Long userId, Pageable pageable) {
        QGroup group = QGroup.group;
        QMembership membership = QMembership.membership;

        // 사용자 ID 기준으로 가입한 모임 목록 조회, joinedAt 기준 정렬
        List<Tuple> joined = queryFactory
                .select(
                        group.id,
                        group.name,
                        group.description,
                        group.coverAttachment.id,
                        group.type,
                        group.interest,
                        membership.joinedAt
                )
                .from(group)
                .join(membership).on(
                        membership.group.eq(group),
                        membership.user.id.eq(userId),
                        membership.joinStatus.eq(JoinStatus.APPROVED)
                )
                .orderBy(membership.joinedAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 조회된 모임 ID 목록 추출
        List<Long> groupIds = joined.stream()
                .map(tuple -> tuple.get(group.id))
                .toList();

        if (groupIds.isEmpty()) {
            // 사용자가 가입한 모임이 없을 경우 빈 페이지 반환
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 각 모임별 가입 회원 수 집계
        List<Tuple> counts = queryFactory
                .select(
                        membership.group.id,
                        membership.id.count()
                )
                .from(membership)
                .where(
                        membership.joinStatus.eq(JoinStatus.APPROVED)
                                .and(membership.group.id.in(groupIds))
                )
                .groupBy(membership.group.id)
                .fetch();

        // 모임 ID + 회원 수 맵 생성
        Map<Long, Long> memberCounts = counts.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(membership.group.id),
                        tuple -> tuple.get(membership.id.count())
                ));

        // 최종 결과 리스트 구성
        List<GroupListResponse> result = new ArrayList<>();

        for (Tuple t : joined) {
            Long groupId = t.get(group.id);
            result.add(new GroupListResponse(
                    groupId,
                    t.get(group.name),
                    t.get(group.description),
                    null,
                    t.get(group.type),
                    t.get(group.interest),
                    memberCounts.getOrDefault(groupId, 0L),
                    t.get(group.coverAttachment.id)
            ));
        }

        // 페이징 처리를 위한 전체 개수 조회
        Long total = queryFactory
                .select(membership.count())
                .from(membership)
                .where(
                        membership.user.id.eq(userId),
                        membership.joinStatus.eq(JoinStatus.APPROVED)
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, total != null ? total : 0L);
    }

    /**
     * 온라인 모임 목록 조회 - 관심사 및 정렬 포함
     *
     * @param sortType 정렬 (인기순/최신순)
     * @param interest 관심사
     * @param pageable 페이징 정보
     * @return 조회된 모임 목록
     */
    @Override
    public Page<GroupListResponse> findOnlineGroups(GroupSortType sortType, GroupInterest interest, Pageable pageable) {
        return findGroupsByCondition(Set.of(GroupType.ONLINE, GroupType.HYBRID), null, sortType, interest, pageable);
    }

    /**
     * 오프라인 모임 목록 조회 - 동네, 관심사, 정렬 포함
     *
     * @param address  사용자 동네 정보
     * @param sortType 정렬 (인기순/최신순)
     * @param interest 관심사
     * @param pageable 페이징 정보
     * @return 조회된 모임 목록
     */
    @Override
    public Page<GroupListResponse> findOfflineGroups(String address, GroupSortType sortType, GroupInterest interest, Pageable pageable) {
        return findGroupsByCondition(Set.of(GroupType.OFFLINE, GroupType.HYBRID), address, sortType, interest, pageable);
    }

    /**
     * 모임 이름 검색 - 관심사, 정렬 포함
     *
     * @param keyword  검색 키워드 (부분 일치)
     * @param interest 관심사
     * @param pageable 페이징 정보
     * @return 조회된 모임 목록
     */
    @Override
    public Page<GroupListResponse> searchGroupsByName(String keyword, GroupInterest interest, Pageable pageable) {
        QGroup group = QGroup.group;
        QMembership membership = QMembership.membership;

        BooleanBuilder conditions = new BooleanBuilder();
        conditions.and(group.access.eq(GroupAccess.PUBLIC)); // 공개 모임만 검색

        // 공백 제거 + 소문자로 변환하여 이름 LIKE 검색
        if (StringUtils.isNotBlank(keyword)) {
            conditions.and(
                    stringTemplate("REPLACE(LOWER({0}), ' ', '')", group.name)
                            .like("%" + keyword.toLowerCase().replace(" ", "") + "%")
            );
        }

        if (interest != null) {
            conditions.and(group.interest.eq(interest)); // 관심사 필터링
        }

        List<Tuple> tuples = queryFactory
                .select(
                        group.id,
                        group.name,
                        group.description,
                        group.type,
                        group.interest,
                        membership.id.countDistinct().as("memberCount"),
                        group.coverAttachment.id
                )
                .from(group)
                .leftJoin(membership).on(
                        membership.group.eq(group),
                        membership.joinStatus.eq(JoinStatus.APPROVED)
                )
                .where(conditions)
                .groupBy(group.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<GroupListResponse> content = tuples.stream()
                .map(t -> toGroupListResponse(t, group, membership))
                .toList();

        Long total = queryFactory
                .select(group.count())
                .from(group)
                .where(conditions)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 조건 기반 모임 목록 조회
     *
     * @param groupTypes 모임 유형 (온/오프라인)
     * @param address    사용자 동네 정보
     * @param sortType   정렬 (인기순/최신순)
     * @param interest   관심사
     * @param pageable   페이징 정보
     * @return 조회된 모임 목록
     */
    private Page<GroupListResponse> findGroupsByCondition(Set<GroupType> groupTypes,
                                                          @Nullable String address,
                                                          GroupSortType sortType,
                                                          GroupInterest interest,
                                                          Pageable pageable) {
        QGroup group = QGroup.group;
        QMembership membership = QMembership.membership;

        BooleanBuilder conditions = buildConditions(groupTypes, interest, address);

        List<Tuple> tuples = queryFactory
                .select(
                        group.id,
                        group.name,
                        group.description,
                        group.type,
                        group.interest,
                        membership.id.countDistinct().as("memberCount"),
                        group.coverAttachment.id
                )
                .from(group)
                .leftJoin(membership).on(
                        membership.group.eq(group),
                        membership.joinStatus.eq(JoinStatus.APPROVED)
                )
                .where(conditions)
                .groupBy(group.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sortType == GroupSortType.POPULAR ?
                        membership.id.count().desc() :
                        group.createdAt.desc())
                .fetch();

        List<GroupListResponse> content = tuples.stream()
                .map(t -> toGroupListResponse(t, group, membership))
                .toList();

        Long total = queryFactory
                .select(group.count())
                .from(group)
                .where(conditions)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanBuilder buildConditions(Set<GroupType> types, GroupInterest interest, String address) {
        QGroup group = QGroup.group;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(group.access.eq(GroupAccess.PUBLIC));

        if (types != null && !types.isEmpty()) {
            builder.and(group.type.in(types));
        }

        if (interest != null) {
            builder.and(group.interest.eq(interest));
        }

        if (StringUtils.isNotBlank(address)) {
            builder.and(group.address.eq(address));
        }

        return builder;
    }

    private GroupListResponse toGroupListResponse(Tuple tuple, QGroup group, QMembership membership) {
        return new GroupListResponse(
                tuple.get(group.id),
                tuple.get(group.name),
                tuple.get(group.description),
                null,
                tuple.get(group.type),
                tuple.get(group.interest),
                tuple.get(membership.id.countDistinct()),
                tuple.get(group.coverAttachment.id)
        );
    }
}
