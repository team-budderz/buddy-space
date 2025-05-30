package team.budderz.buddyspace.global.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.infra.database.user.entity.UserRole;

@Getter
@RequiredArgsConstructor
public class UserAuth {
    private final Long userId;
    private final UserRole role;
}
