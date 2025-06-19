package team.budderz.buddyspace.infra.database.group.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import team.budderz.buddyspace.api.group.response.GroupListResponse;
import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;
import team.budderz.buddyspace.infra.database.group.entity.GroupSortType;

public interface GroupQueryRepository {

    Page<GroupListResponse> findGroupsByUser(Long userId, Pageable pageable);

    Page<GroupListResponse> findOnlineGroups(GroupSortType sortType, GroupInterest interest, Pageable pageable);

    Page<GroupListResponse> findOfflineGroups(String address, GroupSortType sortType, GroupInterest interest, Pageable pageable);

    Page<GroupListResponse> searchGroupsByName(String keyword, GroupInterest interest, Pageable pageable);
}
