package team.budderz.buddyspace.infra.database.group.repository;

import team.budderz.buddyspace.api.group.response.GroupListResponse;
import team.budderz.buddyspace.infra.database.group.entity.GroupSortType;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;

import java.util.List;

public interface GroupQueryRepository {

    List<GroupListResponse> findGroupsByUser(Long userId);

    List<GroupListResponse> findOnlineGroups(GroupSortType sortType);

    List<GroupListResponse> findOfflineGroups(Neighborhood neighborhood, GroupSortType sortType);
}
