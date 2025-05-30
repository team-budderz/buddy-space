package team.budderz.buddyspace.domain.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.group.response.SaveGroupResponse;
import team.budderz.buddyspace.domain.group.exception.GroupErrorCode;
import team.budderz.buddyspace.domain.group.exception.GroupException;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupAccess;
import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    /**
     * 모임 생성
     *
     * @param userId 로그인 사용자 ID
     * @param name 모임 이름
     * @param access 모임 공개 범위
     * @param type 모임 유형
     * @param interest 모임 관심사
     * @return 생성된 모임 정보(이름, 공개 범위, 유형, 관심사)
     */
    @Transactional
    public SaveGroupResponse saveGroup (
            Long userId,
            String name,
            GroupAccess access,
            GroupType type,
            GroupInterest interest
    ) {
        User leader = userRepository.findById(userId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.USER_NOT_FOUND));
//                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Group group = new Group(name, access, type, interest, leader);
        Group saved = groupRepository.save(group);

        return SaveGroupResponse.from(saved);
    }
}
