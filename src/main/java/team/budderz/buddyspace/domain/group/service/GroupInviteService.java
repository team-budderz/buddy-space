package team.budderz.buddyspace.domain.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.group.response.GroupInviteResponse;
import team.budderz.buddyspace.domain.group.util.CodeGenerator;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;

@Service
@RequiredArgsConstructor
public class GroupInviteService {

    private final GroupValidator validator;

    @Value("${app.invite.base.url}")
    private String inviteBaseUrl;

    /**
     * 모임 초대 링크 생성
     *
     * @param loginUserId 로그인 사용자 ID
     * @param groupId     모임 ID
     * @return 생성된 초대 링크 정보
     */
    @Transactional
    public GroupInviteResponse updateInviteLink(Long loginUserId, Long groupId) {
        // 초대 링크 생성 권한 검증
        validator.validatePermission(loginUserId, groupId, PermissionType.CREATE_INVITE_LINK);
        Group group = validator.findGroupOrThrow(groupId);

        // 초대 링크가 이미 존재하면 바로 반환
        if (group.getInviteCode() != null) {
            return GroupInviteResponse.of(group, inviteBaseUrl + group.getInviteCode());
        }

        // 초대 링크 생성 (중복 방지)
        String code;
        do {
            code = CodeGenerator.generate();
        } while (validator.isExistsGroupByCode(code));

        group.updateInviteCode(code);

        return GroupInviteResponse.of(group, inviteBaseUrl + code);
    }

    /**
     * 모임의 초대 링크 조회 (관리용)
     *
     * @param loginUserId 로그인 사용자 ID (리더)
     * @param groupId     모임 ID
     * @return 조회된 초대 링크 정보
     */
    @Transactional(readOnly = true)
    public GroupInviteResponse findInviteLink(Long loginUserId, Long groupId) {
        validator.validateLeader(loginUserId, groupId);
        Group group = validator.findGroupOrThrow(groupId);

        String inviteLink = group.getInviteCode() == null ? null : inviteBaseUrl + group.getInviteCode();

        return GroupInviteResponse.of(group, inviteLink);
    }

    /**
     * 모임의 초대 링크 삭제 (관리용)
     *
     * @param loginUserId 로그인 사용자 ID (리더)
     * @param groupId     모임 ID
     * @return 삭제된 초대 링크 정보
     */
    @Transactional
    public GroupInviteResponse deleteInviteLink(Long loginUserId, Long groupId) {
        validator.validateLeader(loginUserId, groupId);
        Group group = validator.findGroupOrThrow(groupId);

        group.updateInviteCode(null);

        return GroupInviteResponse.of(group, null);
    }
}
