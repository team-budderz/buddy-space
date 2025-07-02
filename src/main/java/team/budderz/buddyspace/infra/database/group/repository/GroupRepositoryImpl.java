package team.budderz.buddyspace.infra.database.group.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.querydsl.core.types.dsl.Expressions.stringTemplate;

/**
 * GroupQueryRepository 인터페이스의 구현체.
 * QueryDSL을 사용해 그룹 조회 기능을 제공합니다.
 */
@Repository
@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupQueryRepository {

    private final JPAQueryFactory queryFactory;

    // Q 인스턴스 재사용
    private static final QGroup qGroup = QGroup.group;
    private static final QMembership qMembership = QMembership.membership;
    private static final QMembership qUserMembership = new QMembership("userMembership");

    // 멤버 수 집계 표현식
    private static final NumberExpression<Long> memberCountExpr = qMembership.id.countDistinct();

    /**
     * 사용자가 가입한 모임 목록을 조회합니다.
     *
     * @param userId   로그인 사용자 ID
     * @param pageable 페이징 정보
     * @return 가입된 모임 목록(Page 형태)
     */
    @Override
    public Page<GroupListResponse> findGroupsByUser(Long userId, Pageable pageable) {
        // 가입된 모임 ID와 정보 조회
        List<Tuple> joinedTuples = queryFactory
                .select(
                        qGroup.id,
                        qGroup.name,
                        qGroup.description,
                        qGroup.coverAttachment.id,
                        qGroup.type,
                        qGroup.interest,
                        qMembership.joinedAt
                )
                .from(qGroup)
                .join(qMembership).on(
                        qMembership.group.eq(qGroup),
                        qMembership.user.id.eq(userId),
                        qMembership.joinStatus.eq(JoinStatus.APPROVED)
                )
                .orderBy(qMembership.joinedAt.desc())   // 가입일 내림차순
                .offset(pageable.getOffset())           // 페이징 오프셋
                .limit(pageable.getPageSize())          // 페이지 크기
                .fetch();

        // 조회된 모임 ID 리스트 추출
        List<Long> groupIds = joinedTuples.stream()
                .map(t -> t.get(qGroup.id))
                .toList();

        if (groupIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 각 모임별 승인된 멤버 수 집계
        List<Tuple> countTuples = queryFactory
                .select(qMembership.group.id, qMembership.id.count())
                .from(qMembership)
                .where(
                        qMembership.joinStatus.eq(JoinStatus.APPROVED)
                                .and(qMembership.group.id.in(groupIds))
                )
                .groupBy(qMembership.group.id)
                .fetch();

        // 모임ID→회원수 맵 생성
        Map<Long, Long> memberCounts = countTuples.stream()
                .collect(Collectors.toMap(
                        t -> t.get(qMembership.group.id),
                        t -> t.get(qMembership.id.count())
                ));

        // GroupListResponse로 매핑
        List<GroupListResponse> content = joinedTuples.stream()
                .map(t -> new GroupListResponse(
                        t.get(qGroup.id),
                        t.get(qGroup.name),
                        t.get(qGroup.description),
                        null,
                        t.get(qGroup.type),
                        t.get(qGroup.interest),
                        memberCounts.getOrDefault(t.get(qGroup.id), 0L),
                        JoinStatus.APPROVED,
                        t.get(qGroup.coverAttachment.id)
                ))
                .toList();

        // 전체 가입 모임 개수 조회
        Long total = queryFactory
                .select(qMembership.count())
                .from(qMembership)
                .where(
                        qMembership.user.id.eq(userId),
                        qMembership.joinStatus.eq(JoinStatus.APPROVED)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 온라인 및 온/오프라인 모임 목록을 조회합니다.
     *
     * @param userId   로그인 사용자 ID
     * @param sortType 인기순 또는 최신순 정렬
     * @param interest 관심사 필터
     * @param pageable 페이징 정보
     * @return 조회된 모임 목록(Page 형태)
     */
    @Override
    public Page<GroupListResponse> findOnlineGroups(Long userId,
                                                    GroupSortType sortType,
                                                    GroupInterest interest,
                                                    Pageable pageable) {
        BooleanBuilder conditions = buildConditions(
                Set.of(GroupType.ONLINE, GroupType.HYBRID),
                interest,
                null
        );
        return queryGroupList(userId, conditions, pageable, sortType);
    }

    /**
     * 오프라인 및 온/오프라인 모임 목록을 조회합니다.
     *
     * @param userId   로그인 사용자 ID
     * @param address  사용자 동네 정보
     * @param sortType 인기순 또는 최신순 정렬
     * @param interest 관심사 필터
     * @param pageable 페이징 정보
     * @return 조회된 모임 목록(Page 형태)
     */
    @Override
    public Page<GroupListResponse> findOfflineGroups(Long userId,
                                                     String address,
                                                     GroupSortType sortType,
                                                     GroupInterest interest,
                                                     Pageable pageable) {
        BooleanBuilder conditions = buildConditions(
                Set.of(GroupType.OFFLINE, GroupType.HYBRID),
                interest,
                address
        );
        return queryGroupList(userId, conditions, pageable, sortType);
    }

    /**
     * 공개(PUBLIC) 모임 중 이름으로 검색합니다.
     *
     * @param userId   로그인 사용자 ID
     * @param keyword  검색 키워드 (공백 무시, 소문자 비교)
     * @param interest 관심사 필터
     * @param pageable 페이징 정보
     * @return 키워드 검색된 모임 목록(Page 형태)
     */
    @Override
    public Page<GroupListResponse> searchGroupsByName(Long userId,
                                                      String keyword,
                                                      GroupInterest interest,
                                                      Pageable pageable) {
        BooleanBuilder conditions = new BooleanBuilder();
        conditions.and(qGroup.access.eq(GroupAccess.PUBLIC));  // 공개 모임만

        if (StringUtils.isNotBlank(keyword)) {
            // 이름 공백 제거 후 소문자 매칭
            conditions.and(stringTemplate(
                            "REPLACE(LOWER({0}), ' ', '')", qGroup.name
                    ).like("%" + keyword.toLowerCase().replace(" ", "") + "%")
            );
        }
        if (interest != null) {
            conditions.and(qGroup.interest.eq(interest));     // 관심사 필터
        }

        return queryGroupList(userId, conditions, pageable, null);
    }

    /**
     * 공통: 공개 모임 목록 조회용 내부 메서드.
     *
     * @param userId     로그인 사용자 ID
     * @param conditions QueryDSL 조건 빌더
     * @param pageable   페이징 정보
     * @param sortType   인기순/최신순 (null이면 정렬 안 함)
     * @return 조회된 모임 목록(Page 형태)
     */
    private Page<GroupListResponse> queryGroupList(Long userId,
                                                   BooleanBuilder conditions,
                                                   Pageable pageable,
                                                   @Nullable GroupSortType sortType) {

        JPAQuery<Tuple> baseQuery = queryFactory
                .select(
                        qGroup.id,
                        qGroup.name,
                        qGroup.description,
                        qGroup.type,
                        qGroup.interest,
                        ExpressionUtils.as(memberCountExpr, "memberCount"),
                        qGroup.coverAttachment.id,
                        qUserMembership.joinStatus
                )
                .from(qGroup)
                .leftJoin(qMembership).on(
                        qMembership.group.eq(qGroup),
                        qMembership.joinStatus.eq(JoinStatus.APPROVED)
                )
                .leftJoin(qUserMembership).on(
                        qUserMembership.group.eq(qGroup),
                        qUserMembership.user.id.eq(userId)
                )
                .where(conditions)
                .groupBy(qGroup.id, qUserMembership.joinStatus);

        if (sortType == GroupSortType.POPULAR) {
            baseQuery.orderBy(memberCountExpr.desc());
        } else if (sortType != null) {
            baseQuery.orderBy(qGroup.createdAt.desc());
        }

        List<Tuple> tuples = baseQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(qGroup.count())
                .from(qGroup)
                .where(conditions)
                .fetchOne();

        List<GroupListResponse> content = tuples.stream()
                .map(t -> new GroupListResponse(
                        t.get(qGroup.id),
                        t.get(qGroup.name),
                        t.get(qGroup.description),
                        null,
                        t.get(qGroup.type),
                        t.get(qGroup.interest),
                        t.get(ExpressionUtils.path(Long.class, "memberCount")),
                        t.get(qUserMembership.joinStatus),
                        t.get(qGroup.coverAttachment.id)
                ))
                .toList();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 공통: 공개 모임 조회용 조건 빌더.
     *
     * @param types    허용할 모임 타입 집합
     * @param interest 관심사 필터 (null 허용)
     * @param address  주소 필터 (null 또는 공백 허용)
     * @return BooleanBuilder 형태의 QueryDSL 조건
     */
    private BooleanBuilder buildConditions(Set<GroupType> types,
                                           GroupInterest interest,
                                           @Nullable String address) {
        
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qGroup.access.eq(GroupAccess.PUBLIC));  // 공개 모임만

        if (types != null && !types.isEmpty()) {
            builder.and(qGroup.type.in(types));             // 타입 필터
        }
        if (interest != null) {
            builder.and(qGroup.interest.eq(interest));      // 관심사 필터
        }
        if (StringUtils.isNotBlank(address)) {
            builder.and(qGroup.address.eq(address));        // 주소 필터
        }
        return builder;
    }
}
