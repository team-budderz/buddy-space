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

    @Transactional
    public GroupInviteResponse updateInviteLink(Long loginUserId, Long groupId) {
        validator.validatePermission(loginUserId, groupId, PermissionType.CREATE_INVITE_LINK);
        Group group = validator.findGroupOrThrow(groupId);

        if (group.getInviteCode() != null) {
            return GroupInviteResponse.of(group, inviteBaseUrl + group.getInviteCode());
        }

        String code;
        do {
            code = CodeGenerator.generate();
        } while (validator.isExistsGroupByCode(code));

        group.updateInviteCode(code);

        return GroupInviteResponse.of(group, inviteBaseUrl + code);
    }

    @Transactional(readOnly = true)
    public GroupInviteResponse findInviteLink(Long loginUserId, Long groupId) {
        validator.validateLeader(loginUserId, groupId);
        Group group = validator.findGroupOrThrow(groupId);

        String inviteLink = group.getInviteCode() == null ? null : inviteBaseUrl + group.getInviteCode();

        return GroupInviteResponse.of(group, inviteLink);
    }

    @Transactional
    public GroupInviteResponse deleteInviteLink(Long loginUserId, Long groupId) {
        validator.validateLeader(loginUserId, groupId);
        Group group = validator.findGroupOrThrow(groupId);

        group.updateInviteCode(null);

        return GroupInviteResponse.of(group, null);
    }
}
